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
pos_x =441;
pos_y =241;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =578;
pos_y =394;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =336;
pos_y =396;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node D(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =687;
pos_y =558;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node E(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =428;
pos_y =559;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node F(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =427;
pos_y =703;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node G(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =691;
pos_y =705;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node H(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =438;
pos_y =83;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

node I(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =826;
pos_y =394;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("presente" "ausente");
}

// Links of the associated graph:

link A B;

link A C;

link B D;

link B E;

link C E;

link D G;

link E F;

link H A;

link I D;

//Network Relationships: 

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

relation F E { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.23 0.88 0.77 0.12 );
}

relation G D { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.25 0.51 0.75 0.49 );
}

relation I { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.15 0.85 );
}

relation D B I { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.14 0.43 0.78 0.9 0.86 0.57 0.22 );
}

relation B A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.74 0.23 0.26 0.77 );
}

relation H { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.32 0.68 );
}

relation A H { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.17 0.91 0.83 0.09 );
}

}
