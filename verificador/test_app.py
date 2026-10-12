import unittest

from app import equivalentes, equivalentes_con_limite


class EquivalentesTest(unittest.TestCase):

    def test_reconoce_la_misma_expresion_escrita_distinto(self):
        self.assertTrue(equivalentes("1+2x", "2*x + 1"))
        self.assertTrue(equivalentes("x^2 - 1", "(x-1)(x+1)"))
        self.assertTrue(equivalentes("y = C1 e^(2x)", "C1*exp(2*x)"))
        self.assertTrue(equivalentes("e^(2x) sen(x)", "sin(x) e^(2x)"))
        self.assertTrue(equivalentes("tg(x)", "sin(x)/cos(x)"))

    def test_distingue_expresiones_distintas(self):
        self.assertFalse(equivalentes("2x+1", "2x-1"))

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
