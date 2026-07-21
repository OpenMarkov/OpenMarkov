# ============================================================
#  Makefile -- rediseno-probnet
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

# ---- Herramientas (sobreescribibles desde la linea de comandos) --

PLANTUML_JAR ?= /home/manuel/eclipse/configuration/org.eclipse.osgi/435/0/.cp/lib/plantuml-epl-1.2024.5.jar
JAVA         ?= java
JAVA_FLAGS   ?=
LATEXMK      ?= latexmk

# ---- Ficheros fuente --------------------------------------------

TEX          := rediseno-probnet.tex
PDF          := rediseno-probnet.pdf
DIAGRAMS_DIR := diagrams

PUML_SOURCES := $(wildcard $(DIAGRAMS_DIR)/*.puml)
PNG_TARGETS  := $(PUML_SOURCES:.puml=.png)

# ---- Targets principales ----------------------------------------

.PHONY: all diagrams pdf watch clean cleanall help

all: pdf

pdf: $(PDF)

$(PDF): $(TEX) $(PNG_TARGETS)
	$(LATEXMK) -pdf -interaction=nonstopmode -halt-on-error $(TEX)

diagrams: $(PNG_TARGETS)

# Regla patron: cada .png se regenera solo si su .puml es mas nuevo
$(DIAGRAMS_DIR)/%.png: $(DIAGRAMS_DIR)/%.puml
	$(JAVA) $(JAVA_FLAGS) -jar $(PLANTUML_JAR) $<

# ---- Modo continuo (recompila al guardar el .tex) ---------------

watch: diagrams
	$(LATEXMK) -pdf -pvc $(TEX)

# ---- Limpieza ---------------------------------------------------

# Elimina auxiliares de LaTeX (.aux, .log, .toc, .out, .fls, .fdb_latexmk)
clean:
	$(LATEXMK) -c $(TEX)

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
	@echo "  watch     Modo continuo: recompila al detectar cambios"
	@echo "  clean     Elimina ficheros auxiliares de LaTeX"
	@echo "  cleanall  Elimina tambien el PDF y los PNGs generados"
	@echo ""
	@echo "Variables configurables (ejemplo: make JAVA=/usr/bin/java):"
	@echo "  PLANTUML_JAR = $(PLANTUML_JAR)"
	@echo "  JAVA         = $(JAVA)"
	@echo "  LATEXMK      = $(LATEXMK)"
	@echo ""
