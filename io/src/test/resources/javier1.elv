// Influence Diagram With Super Value Nodes
//   Elvira format 

id-with-svnodes  "Untitled3" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node A(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =250;
pos_y =204;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node B(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =557;
pos_y =194;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =412;
pos_y =187;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node U(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =406;
pos_y =510;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
}

node U1(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =257;
pos_y =324;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
}

node U2(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =399;
pos_y =321;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
}

node U3(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =560;
pos_y =318;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
}

node U4(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =479;
pos_y =420;
relevance = 7.0;
purpose = "";
min = 0.0;
max = 1.0;
precision = 2;
}

// Links of the associated graph:

link A U1;

link A U2;

link D U2;

link B D;

link B U3;

link U2 U4;

link U3 U4;

link U1 U;

link U4 U;

//Network Relationships: 

relation A { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.5 0.5 );
}

relation B { 
comment = "";
deterministic=false;
values= table (0.5 0.5 );
}

relation U1 A { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (1.0 2.0 );
}

relation U2 A D { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (3.0 4.0 5.0 6.0 );
}

relation U3 B { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (7.0 8.0 );
}

relation U4 U2 U3 { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Sum();}

relation U U1 U4 { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Sum();}

}
