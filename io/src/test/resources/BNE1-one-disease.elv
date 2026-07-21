// Bayesian Network
//   Elvira format 

bnet  "Untitled4" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node D(finite-states) {
title = "Disease";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =170;
pos_y =106;
relevance = 10.0;
purpose = "Disease";
num-states = 2;
states = ("present" "absent");
}

node E(finite-states) {
title = "Symptom";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =218;
pos_y =274;
relevance = 7.0;
purpose = "Symptom";
num-states = 2;
states = ("present" "absent");
}

// Links of the associated graph:

link D E;

//Network Relationships: 

relation D { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.03 0.97 );
}

relation E D { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.95 0.02 0.05 0.98 );
}

}
