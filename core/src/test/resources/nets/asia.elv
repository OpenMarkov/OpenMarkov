// Bayesian Network
//   Elvira format 

bnet  "Asia" { 

// Network Properties

kindofgraph = "directed";
title = "Ejemplo de red clasica en la literatura";
author = "Lauritzen y Spiegelhalter";
whochanged = "Jose A. Gamez";
whenchanged = "19/08/99";
visualprecision = "0.00";
version = 1.0;
default node states = (yes , no);

// Variables 

node X(finite-states) {
title = "Positive X-ray?";
comment = "Indica si el test de rayos X ha sido positivo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =252;
pos_y =322;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node B(finite-states) {
title = "Has bronchitis";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =698;
pos_y =181;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node D(finite-states) {
title = "Dyspnoea?";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =533;
pos_y =321;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node A(finite-states) {
title = "Visit to Asia?";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =290;
pos_y =58;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node S(finite-states) {
title = "Smoker?";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =568;
pos_y =52;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node L(finite-states) {
title = "Has lung cancer";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =421;
pos_y =152;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node T(finite-states) {
title = "Has tuberculosis";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =201;
pos_y =150;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node E(finite-states) {
title = "Tuberculosis or cancer";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =336;
pos_y =238;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

// Links of the associated graph:

link A T;

link B D;

link E D;

link E X;

link L E;

link S B;

link S L;

link T E;

//Network Relationships: 

relation A { 
comment = "Probabilidades a priori de haber visitado Asia";
deterministic=false;
values= table (0.01 0.99 );
}

relation S { 
comment = "Probabilidades a priori de ser fumador";
deterministic=false;
values= table (0.5 0.5 );
}

relation T A { 
comment = "P(T|A)";
deterministic=false;
values= table (0.05 0.01 0.95 0.99 );
}

relation L S { 
comment = "P(L|S)";
deterministic=false;
values= table (0.1 0.01 0.9 0.99 );
}

relation B S { 
comment = "P(B|S)";
deterministic=false;
values= table (0.6 0.3 0.4 0.7 );
}

relation E L T { 
comment = "P(E|L,T)";
deterministic=false;
values= table (1.0 1.0 1.0 0.0 0.0 0.0 0.0 1.0 );
}

relation X E { 
comment = "P(X|E)";
deterministic=false;
values= table (0.98 0.05 0.02 0.95 );
}

relation D E B { 
comment = "P(D|E,B)";
deterministic=false;
values= table (0.9 0.7 0.8 0.1 0.1 0.3 0.2 0.9 );
}

}
