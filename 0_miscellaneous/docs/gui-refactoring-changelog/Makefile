# ============================================================
#  Makefile -- gui-refactoring-changelog
#  Compiles PlantUML diagrams (.puml -> .png) and generates
#  the PDF with LaTeX.
#
#  Usage:
#    make            build everything (PNGs + PDF)
#    make diagrams   only regenerate PNGs from .puml files
#    make pdf        only regenerate the PDF (assumes PNGs are current)
#    make clean      remove LaTeX auxiliary files
#    make cleanall   also remove PDF and generated PNGs
# ============================================================

PLANTUML     ?= plantuml
PDFLATEX     ?= pdflatex

TEX          := gui-refactoring-changelog.tex
PDF          := gui-refactoring-changelog.pdf
DIAGRAMS_DIR := diagrams

PUML_SOURCES := $(wildcard $(DIAGRAMS_DIR)/*.puml)
PNG_TARGETS  := $(PUML_SOURCES:.puml=.png)

.PHONY: all diagrams pdf clean cleanall

all: pdf

pdf: $(PDF)

$(PDF): $(TEX) $(PNG_TARGETS)
	$(PDFLATEX) -interaction=nonstopmode -halt-on-error $(TEX)
	$(PDFLATEX) -interaction=nonstopmode -halt-on-error $(TEX)

diagrams: $(PNG_TARGETS)

$(DIAGRAMS_DIR)/%.png: $(DIAGRAMS_DIR)/%.puml
	$(PLANTUML) -tpng $<

clean:
	rm -f $(TEX:.tex=.aux) $(TEX:.tex=.log) $(TEX:.tex=.toc) \
	      $(TEX:.tex=.out) $(TEX:.tex=.fls) $(TEX:.tex=.fdb_latexmk)

cleanall: clean
	rm -f $(PDF) $(PNG_TARGETS)
