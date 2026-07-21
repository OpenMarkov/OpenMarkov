// Bayesian Network
//   Elvira format 

bnet  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = (presente , ausente);

// Variables 

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =275;
pos_y =206;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (presente ausente);
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =479;
pos_y =217;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (presente ausente);
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =390;
pos_y =355;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

// Links of the associated graph:

link A C;

link B C;

//Network Relationships: 

relation A { 
comment = "new";
deterministic=false;
values= table (0.5 0.5 );
}

relation B { 
comment = "new";
deterministic=false;
values= table (0.5 0.5 );
}

relation C A B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.2 0.3 0.4 0.9 0.8 0.7 0.6 );
}

}
