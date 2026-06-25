// Bayesian Network
//   Elvira format 

bnet  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00000000";
version = 1.0;
default node states = (presente , ausente);

// Variables 

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =212;
pos_y =79;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =626;
pos_y =169;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =302;
pos_y =199;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node D(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =705;
pos_y =323;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node E(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =440;
pos_y =321;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

// Links of the associated graph:

link A C;

link B D;

link B E;

link C E;

//Network Relationships: 

relation A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.9 0.1 );
}

relation B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.87 0.13 );
}

relation D B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.54 0.69 0.46 0.31 );
}

relation C A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.02 0.19 0.98 0.81 );
}

relation E B C { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.21 0.32 0.76 0.98 0.79 0.68 0.24 0.02 );
}

}
