// Influence Diagram
//   Elvira format 

idiagram  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =401;
pos_y =176;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =443;
pos_y =380;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node C(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =557;
pos_y =82;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D2(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =500;
pos_y =486;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node D1(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =404;
pos_y =270;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node U(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =619;
pos_y =545;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link A D1;

link B D2;

link C A;

link C B;

link C U;

link D1 B;

link D2 U;

//Network Relationships: 

relation C { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.9 );
}

relation A C { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.7 0.2 0.3 0.8 );
}

relation B C D1 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.2 0.8 0.1 0.6 0.8 0.2 0.9 0.4 );
}

relation U C D2 { 
comment = "new";
kind-of-relation = utility;
deterministic=false;
values= table (5.0 0.0 4.0 7.0 );
}

}
