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
pos_x =109;
pos_y =82;
relevance = 9.0;
purpose = "Disease";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
title = "Virus B";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =311;
pos_y =83;
relevance = 9.0;
purpose = "Disease";
num-states = 2;
states = ("present" "absent");
}

node C(finite-states) {
title = "Disease 1";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =211;
pos_y =214;
relevance = 10.0;
purpose = "Disease";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
title = "Disease 2";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =512;
pos_y =215;
relevance = 10.0;
purpose = "Disease";
num-states = 2;
states = ("present" "absent");
}

node E(finite-states) {
title = "Symptom";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =357;
pos_y =319;
relevance = 7.0;
purpose = "Symptom";
num-states = 2;
states = ("present" "absent");
}

node F(finite-states) {
title = "Sign";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =120;
pos_y =414;
relevance = 7.0;
purpose = "Sign";
num-states = 2;
states = ("present" "absent");
}

node H(finite-states) {
title = "X-ray";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =493;
pos_y =418;
relevance = 7.0;
purpose = "Test";
num-states = 2;
states = ("positive" "negative");
}

node I(finite-states) {
title = "Ecography";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =701;
pos_y =417;
relevance = 7.0;
purpose = "Test";
num-states = 2;
states = ("positive" "negative");
}

node J(finite-states) {
title = "Vaccination";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =552;
pos_y =82;
relevance = 7.0;
purpose = "Factor_de_riesgo";
num-states = 2;
states = ("yes" "no");
}

node G(finite-states) {
title = "Anomaly";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =617;
pos_y =316;
relevance = 4.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
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
deterministic=false;
values= table (0.99 0.9 0.9 0.0 0.01 0.1 0.1 1.0 );
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
values= table (0.7 0.01 0.3 0.99 );
}

relation G D { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (1.0 0.01 0.0 0.99 );
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
