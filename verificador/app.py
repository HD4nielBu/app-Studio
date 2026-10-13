"""Verificador matemático: dice si dos expresiones son equivalentes con SymPy.

POST /equivalentes {"esperada": "...", "dada": "..."} -> {"equivalentes": true | false | null}
null significa "no se pudo decidir" (no se entiende la expresión o tardó demasiado).
"""
import json
import multiprocessing
from itertools import permutations
import re
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

from sympy import E, simplify, sin, tan
from sympy.parsing.sympy_parser import (_token_splittable, convert_xor, function_exponentiation, implicit_application,
                                        implicit_multiplication, parse_expr, split_symbols_custom,
                                        standard_transformations)

MAX_LARGO = 300
SEGUNDOS = 5
# parse_expr usa eval: solo se admiten caracteres de fórmula y nada de "__" ni ";" para cerrar el paso a código.
PERMITIDOS = re.compile(r"[0-9A-Za-z_+\-*/^().,=\s]*")
# Como implicit_multiplication_application, pero C1 sigue siendo una constante y no C*1.
SEPARAR = split_symbols_custom(lambda nombre: not re.fullmatch(r"[A-Za-z]\d+", nombre) and _token_splittable(nombre))
TRANSFORMACIONES = standard_transformations + (SEPARAR, implicit_multiplication, implicit_application,
                                               function_exponentiation, convert_xor)
# Solo el espacio de nombres de SymPy, sin las funciones nativas de Python (eval, chr, open...).
NOMBRES = {}
exec("from sympy import *", NOMBRES)
NOMBRES["__builtins__"] = {}
# notación en español: sen y tg
LOCALES = {"e": E, "sen": sin, "tg": tan}


def a_expresion(texto):
    if len(texto) > MAX_LARGO or not PERMITIDOS.fullmatch(texto) or "__" in texto:
        raise ValueError("expresión no admitida")
    # shortcut: de "y = 2x" solo se compara el lado derecho; no sirve para ecuaciones implícitas (porción 3, IA).
    return parse_expr(texto.split("=")[-1], transformations=TRANSFORMACIONES,
                      global_dict=NOMBRES, local_dict=LOCALES)


def equivalentes(esperada, dada):
    """True, False o None si no se puede decidir."""
    try:
        a, b = a_expresion(esperada), a_expresion(dada)
        if simplify(a - b) == 0:
            return True
        return _iguales_renombrando_constantes(a, b)
    except Exception:
        return None


def _iguales_renombrando_constantes(a, b):
    # Las constantes arbitrarias (C1, C2...) pueden llevar otro nombre u orden: A e^-x + B e^x vale lo mismo.
    # shortcut: no reconoce constantes reescaladas (2*C1, C1+C2); ampliar si los ejercicios lo piden.
    constantes = sorted((s for s in a.free_symbols if re.fullmatch(r"C\d*", s.name)), key=lambda s: s.name)
    variables = a.free_symbols - set(constantes)
    del_alumno = sorted(b.free_symbols - variables, key=lambda s: s.name)
    if not constantes or len(del_alumno) != len(constantes) or len(constantes) > 4:
        return False
    return any(simplify(a - b.xreplace(dict(zip(del_alumno, orden)))) == 0
               for orden in permutations(constantes))


def _en_proceso(esperada, dada, salida):
    salida.put(equivalentes(esperada, dada))


def equivalentes_con_limite(esperada, dada, segundos=SEGUNDOS):
    # simplify puede no terminar nunca: se corre en un proceso aparte que se mata al vencer el tiempo.
    salida = multiprocessing.Queue()
    proceso = multiprocessing.Process(target=_en_proceso, args=(esperada, dada, salida))
    proceso.start()
    proceso.join(segundos)
    if proceso.is_alive():
        proceso.kill()
        return None
    return salida.get() if not salida.empty() else None


class Manejador(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path != "/equivalentes":
            return self._responder(404, {"error": "ruta desconocida"})
        try:
            cuerpo = json.loads(self.rfile.read(int(self.headers.get("Content-Length", 0))))
            esperada, dada = cuerpo["esperada"], cuerpo["dada"]
            if not isinstance(esperada, str) or not isinstance(dada, str):
                raise ValueError
        except (ValueError, KeyError, TypeError):
            return self._responder(400, {"error": "se esperaba {\"esperada\": texto, \"dada\": texto}"})
        self._responder(200, {"equivalentes": equivalentes_con_limite(esperada, dada)})

    def _responder(self, estado, datos):
        cuerpo = json.dumps(datos).encode()
        self.send_response(estado)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(cuerpo)))
        self.end_headers()
        self.wfile.write(cuerpo)


if __name__ == "__main__":
    ThreadingHTTPServer(("0.0.0.0", 8000), Manejador).serve_forever()
