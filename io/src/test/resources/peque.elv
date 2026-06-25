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
pos_x =459;
pos_y =104;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =622;
pos_y =223;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =373;
pos_y =335;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

// Links of the associated graph:

link A B;

link A C;

link B C;

//Network Relationships: 

relation A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.8 0.2 );
}

relation B A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.3 0.9 0.7 );
}

relation C A B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.02 0.16 0.71 0.85 0.98 0.84 0.29 0.15 );
}

}
