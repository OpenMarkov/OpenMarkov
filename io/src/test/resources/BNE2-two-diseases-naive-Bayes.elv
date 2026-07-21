// Bayesian Network
//   Elvira format 

bnet  "Untitled4" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node A(finite-states) {
title = "Virus A";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =117;
pos_y =395;
relevance = 9.0;
purpose = "Disease";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
title = "Virus B";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =319;
pos_y =407;
relevance = 9.0;
purpose = "Disease";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
title = "Diagnosis";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =414;
pos_y =165;
relevance = 10.0;
purpose = "Disease";
num-states = 5;
states = ("disease 1 - present" "disease 2 - severe" "disease 2 - moderate" "disease 2 - mild" "no disease");
}

node E(finite-states) {
title = "Symptom";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =531;
pos_y =405;
relevance = 7.0;
purpose = "Symptom";
num-states = 2;
states = ("present" "absent");
}

node F(finite-states) {
title = "Sign";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =733;
pos_y =403;
relevance = 7.0;
purpose = "Sign";
num-states = 2;
states = ("present" "absent");
}

node H(finite-states) {
title = "X-ray";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =827;
pos_y =241;
relevance = 7.0;
purpose = "Test";
num-states = 2;
states = ("present" "absent");
}

node I(finite-states) {
title = "Ecography";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =800;
pos_y =322;
relevance = 7.0;
purpose = "Test";
num-states = 2;
states = ("present" "absent");
}

node J(finite-states) {
title = "Familiar antecedents";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =104;
pos_y =286;
relevance = 7.0;
purpose = "Factor_de_riesgo";
num-states = 2;
states = ("present" "absent");
}

// Links of the associated graph:

link D A;

link D B;

link D E;

link D F;

link D H;

link D I;

link D J;

//Network Relationships: 

relation D { 
comment = "";
deterministic=false;
values= table (0.2 0.2 0.2 0.2 0.2 );
}

relation J D { 
comment = "";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation A D { 
comment = "";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation B D { 
comment = "";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation E D { 
comment = "";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation F D { 
comment = "";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation I D { 
comment = "";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

relation H D { 
comment = "";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

}
