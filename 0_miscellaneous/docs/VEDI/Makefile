# ──────────────────────────────────────────────────────────────────────
# Makefile — Documentación VE para Diagramas de Influencia
# ──────────────────────────────────────────────────────────────────────

TEX      = pdflatex
PLANTUML = plantuml

DOC      = ve-influence-diagrams
PUML     = $(DOC).puml

# PlantUML genera un PNG por cada @startuml con nombre distinto
UML_DIAGRAMS = $(DOC).png $(DOC)-activity.png $(DOC)-sequence.png

.PHONY: all pdf uml clean distclean

all: pdf uml

# ── LaTeX (dos pasadas para TOC) ─────────────────────────────────────

pdf: $(DOC).pdf

$(DOC).pdf: $(DOC).tex $(UML_DIAGRAMS)
	$(TEX) $(DOC).tex
	$(TEX) $(DOC).tex

# ── PlantUML ─────────────────────────────────────────────────────────

uml: $(UML_DIAGRAMS)

$(UML_DIAGRAMS): $(PUML)
	$(PLANTUML) $(PUML)

# ── Limpieza ─────────────────────────────────────────────────────────

clean:
	rm -f $(DOC).aux $(DOC).log $(DOC).toc $(DOC).out $(DOC).nav \
	      $(DOC).snm $(DOC).vrb

distclean: clean
	rm -f $(DOC).pdf $(UML_DIAGRAMS)
