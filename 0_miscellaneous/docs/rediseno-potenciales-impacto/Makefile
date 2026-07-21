# ============================================================
#  Makefile -- rediseno-potenciales-impacto
#  Compila los diagramas PlantUML (.puml -> .png) y genera
#  el PDF con LaTeX.
#
#  Uso habitual:
#    make            genera todo (PNGs + PDF)
#    make diagrams   solo regenera los PNGs desde los .puml
#    make pdf        solo regenera el PDF (asume PNGs al dia)
#    make watch      modo continuo: recompila al detectar cambios
#    make clean      elimina ficheros auxiliares de LaTeX
#    make cleanall   elimina tambien el PDF y los PNGs generados
#    make help       muestra esta ayuda
# ============================================================

PLANTUML      ?= plantuml
LATEXMK       ?= latexmk

TEX           := rediseno-potenciales-impacto.tex
PDF           := rediseno-potenciales-impacto.pdf
DIAGRAMS_DIR  := diagrams

PUML_SOURCES  := $(wildcard $(DIAGRAMS_DIR)/*.puml)
PNG_TARGETS   := $(PUML_SOURCES:.puml=.png)

.PHONY: all diagrams pdf watch clean cleanall help

all: pdf

pdf: $(PDF)

$(PDF): $(TEX) $(PNG_TARGETS)
	$(LATEXMK) -pdf -interaction=nonstopmode -halt-on-error $(TEX)

diagrams: $(PNG_TARGETS)

$(DIAGRAMS_DIR)/%.png: $(DIAGRAMS_DIR)/%.puml
	$(PLANTUML) -tpng $<

watch: diagrams
	$(LATEXMK) -pdf -pvc $(TEX)

clean:
	$(LATEXMK) -c $(TEX)

cleanall: clean
	rm -f $(PDF) $(PNG_TARGETS)

help:
	@echo ""
	@echo "Targets disponibles:"
	@echo "  all       Genera PNGs y PDF (target por defecto)"
	@echo "  diagrams  Regenera solo los PNGs desde los .puml"
	@echo "  pdf       Regenera solo el PDF"
	@echo "  watch     Modo continuo: recompila al detectar cambios"
	@echo "  clean     Elimina ficheros auxiliares de LaTeX"
	@echo "  cleanall  Elimina tambien el PDF y los PNGs generados"
	@echo ""
