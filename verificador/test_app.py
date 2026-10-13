import unittest

from app import equivalentes, equivalentes_con_limite


class EquivalentesTest(unittest.TestCase):

    def test_reconoce_la_misma_expresion_escrita_distinto(self):
        self.assertTrue(equivalentes("1+2x", "2*x + 1"))
        self.assertTrue(equivalentes("x^2 - 1", "(x-1)(x+1)"))
        self.assertTrue(equivalentes("y = C1 e^(2x)", "C1*exp(2*x)"))
        self.assertTrue(equivalentes("e^(2x) sen(x)", "sin(x) e^(2x)"))
        self.assertTrue(equivalentes("tg(x)", "sin(x)/cos(x)"))

    def test_acepta_constantes_con_otro_nombre_u_orden(self):
        self.assertTrue(equivalentes("C1*exp(x) + C2*exp(-x)", "C1*exp(-x) + C2*exp(x)"))
        self.assertTrue(equivalentes("C1*exp(x) + C2*exp(-x)", "A*exp(-x) + B*exp(x)"))

    def test_distingue_expresiones_distintas(self):
        self.assertFalse(equivalentes("2x+1", "2x-1"))
        # una sola constante no alcanza para la solución general
        self.assertFalse(equivalentes("C1*exp(x) + C2*exp(-x)", "A*exp(x) + A*exp(-x)"))
        self.assertFalse(equivalentes("C1*exp(x) + C2*exp(-x)", "A*exp(x) + B*exp(2x)"))
        # C1 y C2 son dos constantes, no C*1 y C*2
        self.assertFalse(equivalentes("C1*exp(x) + C2*exp(-x)", "C*exp(x) + 2*C*exp(-x)"))

    def test_no_decide_si_no_entiende_la_expresion(self):
        self.assertIsNone(equivalentes("2x+", "2x"))

    def test_rechaza_codigo(self):
        self.assertIsNone(equivalentes("__import__('os')", "1"))
        self.assertIsNone(equivalentes("1; 2", "1"))
        # sin las funciones nativas, eval y chr son solo símbolos: eval*chr*49 no vale 1
        self.assertFalse(equivalentes("eval(chr(49))", "1"))

    def test_corta_si_tarda_demasiado(self):
        self.assertIsNone(equivalentes_con_limite("x", "x", segundos=0))


if __name__ == "__main__":
    unittest.main()
