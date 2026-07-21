// Bayesian Network
//   Elvira format 

bnet  "Untitled4" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.000";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node A(finite-states) {
title = "Virus A";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =107;
pos_y =70;
relevance = 9.0;
purpose = "Disease";
num-states = 2;
states = ("presente" "ausente");
}

node B(finite-states) {
title = "Virus B";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =311;
pos_y =70;
relevance = 9.0;
purpose = "Disease";
num-states = 2;
states = ("presente" "ausente");
}

node C(finite-states) {
title = "Enfermedad 1";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =209;
pos_y =202;
relevance = 10.0;
purpose = "Disease";
num-states = 2;
states = ("presente" "ausente");
}

node D(finite-states) {
title = "Enfermedad 2";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =509;
pos_y =204;
relevance = 10.0;
purpose = "Disease";
num-states = 2;
states = ("presente" "ausente");
}

node E(finite-states) {
title = "Síntoma";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =355;
pos_y =307;
relevance = 7.0;
purpose = "Symptom";
num-states = 2;
states = ("presente" "ausente");
}

node F(finite-states) {
title = "Signo";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =118;
pos_y =402;
relevance = 7.0;
purpose = "Sign";
num-states = 3;
states = ("severo" "leve" "ausente");
}

node H(finite-states) {
title = "Radiografía";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =491;
pos_y =406;
relevance = 7.0;
purpose = "Test";
num-states = 2;
states = ("presente" "ausente");
}

node I(finite-states) {
title = "Ecografía";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =760;
pos_y =405;
relevance = 7.0;
purpose = "Test";
num-states = 2;
states = ("presente" "ausente");
}

node J(finite-states) {
title = "Vacuna";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =550;
pos_y =70;
relevance = 7.0;
purpose = "Factor_de_riesgo";
num-states = 2;
states = ("si" "no");
}

node G(finite-states) {
title = "Anomalía";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =615;
pos_y =303;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

// Links of the associated graph:

link A C;

link B C;

link C E;

link C F;

link D E;

link D G;

link G H;

link G I;

link J D;

//Network Relationships: 

relation A { 
comment = "";
deterministic=false;
values= table (0.02 0.98 );
}

relation B { 
comment = "";
deterministic=false;
values= table (0.01 0.99 );
}

relation C A B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.99 0.9 0.8 0.01 0.01 0.1 0.2 0.99 );
}

relation E C D { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (1.0 0.9604 0.9307 0.01 0.0 0.0396 0.0693 0.99 );
}

relation J { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.8 0.2 );
}

relation D J { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.01 0.05 0.99 0.95 );
}

relation F C { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.7 0.0010 0.2 0.025 0.1 0.974 );
}

relation G D { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.8 0.01 0.2 0.99 );
}

relation H G { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.8 0.02 0.2 0.98 );
}

relation I G { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.86 0.05 0.14 0.95 );
}

}
