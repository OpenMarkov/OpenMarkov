// Influence Diagram
//   Elvira format 

idiagram  "" { 

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
pos_x =134;
pos_y =139;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node D(finite-states) {
title = "Therapy";
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =406;
pos_y =139;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("yes" "no");
}

node U(continuous) {
title = "Health state";
kind-of-node = utility;
type-of-variable = continuous;
pos_x =281;
pos_y =338;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link D U;

link X U;

//Network Relationships: 

relation X { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= generalizedTable (0.14|range(0.1,0.5)|"prevalence de X" # );
}

relation U X D { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= generalizedTable (8.0|range(7.5,8.5) 3.0|range(2.3,3.5) 9.0|range(8.5,9.5) 10.0 );
}

}
