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
pos_x =203;
pos_y =100;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (presente ausente);
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =411;
pos_y =93;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (presente ausente);
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =208;
pos_y =238;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (presente ausente);
}

// Links of the associated graph:

link B A;

link B C;

link C A;

//Network Relationships: 

relation B { 
comment = "new";
deterministic=false;
values= table (0.5 0.5 );
}

relation C B { 
comment = "new";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 );
}

relation A B C { 
comment = "new";
deterministic=false;
values= table (0.5 0.5 0.5 0.5 0.5 0.5 0.5 0.5 );
}

}
