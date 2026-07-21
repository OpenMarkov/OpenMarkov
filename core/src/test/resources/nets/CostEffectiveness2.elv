// Influence Diagram
//   Elvira format 

idiagram  "Untitled2" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node X(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =132;
pos_y =114;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =284;
pos_y =116;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node E(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =185;
pos_y =221;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node C(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =304;
pos_y =221;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link X D;

link X E;

link D E;

link D C;

//Network Relationships: 

relation X { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.1 0.9 );
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
