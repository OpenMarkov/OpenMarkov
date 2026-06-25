# Makefile para compilar el informe LaTeX con diagramas PlantUML
#
# Prerrequisitos:
#   - plantuml (disponible como `plantuml` en PATH, o ajustar PLANTUML abajo)
#   - pdflatex (TeX Live o MiKTeX)
#
# Uso:
#   make          -> genera todo (diagramas + PDF)
#   make diagrams -> sólo genera los PNG de PlantUML
#   make pdf      -> sólo compila el PDF (asume que los PNG ya existen)
#   make clean    -> borra archivos intermedios
#   make distclean-> borra todo lo generado

PLANTUML   ?= plantuml
PDFLATEX   ?= pdflatex
MAIN       := informe
MAIN2      := jerarquia-excepciones

PUML_SRCS  := sax_before_after.puml \
              nonprojectable_hierarchy.puml \
              runtime_hierarchy.puml \
              hierarchy_checked.puml \
              hierarchy_unchecked.puml

PUML_PNGS  := $(PUML_SRCS:.puml=.png)

.PHONY: all diagrams pdf pdf2 clean distclean

all: diagrams pdf pdf2

diagrams: $(PUML_PNGS)

%.png: %.puml
	$(PLANTUML) -tpng $<

pdf: diagrams
	$(PDFLATEX) -interaction=nonstopmode $(MAIN).tex
	$(PDFLATEX) -interaction=nonstopmode $(MAIN).tex

pdf2: diagrams
	$(PDFLATEX) -interaction=nonstopmode $(MAIN2).tex
	$(PDFLATEX) -interaction=nonstopmode $(MAIN2).tex

clean:
	rm -f *.aux *.log *.toc *.out

distclean: clean
	rm -f $(PUML_PNGS) $(MAIN).pdf $(MAIN2).pdf
