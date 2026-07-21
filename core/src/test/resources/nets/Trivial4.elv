// Influence Diagram
//   Elvira format 

idiagram  "" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.0000";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node D1(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =163;
pos_y =119;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node D2(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =707;
pos_y =449;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node U(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =404;
pos_y =449;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =405;
pos_y =119;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =707;
pos_y =119;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node T(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =707;
pos_y =276;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

// Links of the associated graph:

link A B;

link A T;

link A U;

link B T;

link D1 A;

link D2 U;

link T D2;

//Network Relationships: 

relation A D1 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.2 0.9 0.8 0.1 );
}

relation B A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.2 0.3 0.8 0.7 );
}

relation U A D2 { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (3.0 2.0 1.0 0.0 );
}

relation T A B { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.0 0.9 0.8 0.7 1.0 0.1 0.2 0.3 );
}

}
