// Bayesian Network
//   Elvira format 

bnet  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = (presente , ausente);

// Variables 

node Tres_estados(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =427;
pos_y =188;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("alto" "medio" "bajo");
}

// Links of the associated graph:

//Network Relationships: 

relation Tres_estados { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.3 0.6 );
}

}
