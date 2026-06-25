// Bayesian Network
//   Elvira format 

bnet  "bayesiana" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = (presente , ausente);

// Variables 

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =142;
pos_y =76;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =87;
pos_y =225;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =153;
pos_y =360;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

// Links of the associated graph:

link A B;

link B C;

//Network Relationships: 

relation A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.9 0.1 );
}

relation B A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.2 0.9 0.8 );
}

relation C B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.3 0.4 0.7 0.6 );
}

}
