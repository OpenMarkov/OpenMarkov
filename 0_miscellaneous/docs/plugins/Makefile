## Makefile para compilar el documento plugins.tex
## Requisitos: pdflatex, java, plantuml.jar
##
## Uso:
##   make              → genera diagramas PNG y compila el PDF
##   make diagrams     → solo genera los PNGs de PlantUML
##   make pdf          → solo compila el PDF (requiere que existan los PNGs)
##   make clean        → borra archivos temporales de LaTeX
##   make cleanall     → borra también los PNGs generados

PLANTUML_JAR ?= /home/manuel/eclipse/configuration/org.eclipse.osgi/435/0/.cp/lib/plantuml-epl-1.2024.5.jar
JAVA         ?= java
PDFLATEX     ?= pdflatex

PUML_DIR  = img
PUML_FILES = $(wildcard $(PUML_DIR)/*.puml)
PNG_FILES  = $(patsubst $(PUML_DIR)/%.puml,$(PUML_DIR)/%.png,$(PUML_FILES))

.PHONY: all diagrams pdf clean cleanall

all: diagrams pdf

diagrams: $(PNG_FILES)

$(PUML_DIR)/%.png: $(PUML_DIR)/%.puml
	$(JAVA) -jar $(PLANTUML_JAR) -tpng -o $(abspath $(PUML_DIR)) $<

pdf: $(PNG_FILES) plugins.tex
	$(PDFLATEX) -interaction=nonstopmode plugins.tex
	$(PDFLATEX) -interaction=nonstopmode plugins.tex

clean:
	rm -f *.aux *.log *.toc *.out *.fls *.fdb_latexmk

cleanall: clean
	rm -f $(PNG_FILES) plugins.pdf
