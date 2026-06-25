// Influence Diagram
//   Elvira format 

idiagram  "Untitled3" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node X(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =169;
pos_y =66;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Y(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =271;
pos_y =126;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =380;
pos_y =199;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node E(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =176;
pos_y =303;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node C(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =415;
pos_y =299;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link X Y;

link X E;

link Y D;

link D E;

link D C;

//Network Relationships: 

relation X { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.9 );
}

relation Y X { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.8 0.3 0.2 0.7 );
}

relation C D { 
comment = "new";
kind-of-relation = utility;
deterministic=false;
values= table (8.0 0.0 );
}

relation E X D { 
comment = "new";
kind-of-relation = utility;
deterministic=false;
values= table (6.0 0.0 9.0 10.0 );
}

}
