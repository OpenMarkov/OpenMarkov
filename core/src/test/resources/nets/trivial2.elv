// Influence Diagram
//   Elvira format 

idiagram  "" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node X(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =75;
pos_y =77;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Y(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =227;
pos_y =129;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =300;
pos_y =208;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node Eff(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =115;
pos_y =311;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Cost(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =300;
pos_y =311;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link D Cost;

link D Eff;

link X Eff;

link X Y;

link Y D;

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
values= table (0.9 0.2 0.1 0.8 );
}

relation Cost D { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (5.0 0.0 );
}

relation Eff X D { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (8.0 1.0 9.0 10.0 );
}

}
