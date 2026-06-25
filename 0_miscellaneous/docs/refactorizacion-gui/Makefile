# ============================================================
#  Makefile -- refactorizacion-gui
#  Compila los diagramas PlantUML (.puml -> .png) y genera
#  el PDF con LaTeX.
#
#  Uso habitual:
#    make            genera todo (PNGs + PDF)
#    make diagrams   solo regenera los PNGs desde los .puml
#    make pdf        solo regenera el PDF (asume PNGs al dia)
#    make clean      elimina ficheros auxiliares de LaTeX
#    make cleanall   elimina tambien el PDF y los PNGs generados
#    make help       muestra esta ayuda
# ============================================================

# ---- Herramientas (sobreescribibles desde la linea de comandos) --

PLANTUML     ?= plantuml
PDFLATEX     ?= pdflatex

# ---- Ficheros fuente --------------------------------------------

TEX          := refactorizacion-gui.tex
PDF          := refactorizacion-gui.pdf
DIAGRAMS_DIR := diagrams

PUML_SOURCES := $(wildcard $(DIAGRAMS_DIR)/*.puml)
PNG_TARGETS  := $(PUML_SOURCES:.puml=.png)

# ---- Targets principales ----------------------------------------

.PHONY: all diagrams pdf clean cleanall help

all: pdf

pdf: $(PDF)

# Dos pasadas de pdflatex para resolver referencias cruzadas y TOC
$(PDF): $(TEX) $(PNG_TARGETS)
	$(PDFLATEX) -interaction=nonstopmode -halt-on-error $(TEX)
	$(PDFLATEX) -interaction=nonstopmode -halt-on-error $(TEX)

diagrams: $(PNG_TARGETS)

# Regla patron: cada .png se regenera solo si su .puml es mas nuevo
$(DIAGRAMS_DIR)/%.png: $(DIAGRAMS_DIR)/%.puml
	$(PLANTUML) -tpng $<

# ---- Limpieza ---------------------------------------------------

# Elimina auxiliares de LaTeX (.aux, .log, .toc, .out)
clean:
	rm -f $(TEX:.tex=.aux) $(TEX:.tex=.log) $(TEX:.tex=.toc) \
	      $(TEX:.tex=.out) $(TEX:.tex=.fls) $(TEX:.tex=.fdb_latexmk)

# Elimina tambien el PDF y todos los PNGs generados desde .puml
cleanall: clean
	rm -f $(PDF) $(PNG_TARGETS)

# ---- Ayuda ------------------------------------------------------

help:
	@echo ""
	@echo "Targets disponibles:"
	@echo "  all       Genera PNGs y PDF (target por defecto)"
	@echo "  diagrams  Regenera solo los PNGs desde los .puml"
	@echo "  pdf       Regenera solo el PDF"
	@echo "  clean     Elimina ficheros auxiliares de LaTeX"
	@echo "  cleanall  Elimina tambien el PDF y los PNGs generados"
	@echo ""
	@echo "Variables configurables (ejemplo: make PDFLATEX=xelatex):"
	@echo "  PLANTUML     = $(PLANTUML)"
	@echo "  PDFLATEX     = $(PDFLATEX)"
	@echo ""
