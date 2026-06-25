# Makefile para compilar el informe de análisis y rediseño de excepciones
#
# Prerrequisitos:
#   - plantuml (disponible como `plantuml` en PATH)
#   - xelatex (TeX Live o MiKTeX)
#
# Uso:
#   make          -> genera todo (diagramas + PDF)
#   make diagrams -> sólo genera los PNG de PlantUML
#   make pdf      -> sólo compila el PDF (asume que los PNG ya existen)
#   make clean    -> borra archivos intermedios
#   make distclean-> borra todo lo generado

PLANTUML   ?= plantuml
XELATEX    ?= xelatex
MAIN       := exception-report

PUML_SRCS  := exception-hierarchy-current-a.puml \
              exception-hierarchy-current-b.puml \
              exception-hierarchy-proposed-checked.puml \
              exception-hierarchy-proposed-unchecked.puml

PUML_PNGS  := $(PUML_SRCS:.puml=.png)

.PHONY: all diagrams pdf clean distclean

all: diagrams pdf

diagrams: $(PUML_PNGS)

%.png: %.puml
	$(PLANTUML) -tpng $<

pdf: diagrams
	$(XELATEX) -interaction=nonstopmode $(MAIN).tex
	$(XELATEX) -interaction=nonstopmode $(MAIN).tex

clean:
	rm -f *.aux *.log *.toc *.out

distclean: clean
	rm -f $(PUML_PNGS) $(MAIN).pdf
