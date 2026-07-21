// Influence Diagram With Super Value Nodes
//   Elvira format 

id-with-svnodes  "" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.000";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node X(finite-states) {
title = "Disease";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =113;
pos_y =55;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Y(finite-states) {
title = "Result of test";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =323;
pos_y =148;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("positive" "negative" "not-performed");
}

node D(finite-states) {
title = "Therapy";
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =387;
pos_y =245;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node U(continuous) {
title = "Health state";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =211;
pos_y =352;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node T(finite-states) {
title = "Do test?";
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =571;
pos_y =75;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node C(continuous) {
title = "Cost of test";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =510;
pos_y =350;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Ug(continuous) {
title = "Global utility";
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =367;
pos_y =452;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link C Ug;

link D U;

link T C;

link T D;

link T Y;

link U Ug;

link X U;

link X Y;

link Y D;

//Network Relationships: 

relation X { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= generalizedTable (0.14|range(0.0,1.0)|"prevalence de X" # );
}

relation Y T X { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= generalizedTable (0.91|range(0.8,0.99)|"sensitivity de Y" # 0.0 0.0 # 0.97|range(0.9,0.99)|"specificity de Y" 0.0 0.0 0.0 0.0 1.0 1.0 );
}

relation U X D { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= generalizedTable (8.0|range(7.5,8.5) 3.0|range(2.3,3.5) 9.0|range(8.5,9.5) 10.0 );
}

relation C T { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= generalizedTable (-0.2|range(-0.25,-0.17)|"cost of test" 0.0 );
}

relation Ug C U { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Sum();}

}
