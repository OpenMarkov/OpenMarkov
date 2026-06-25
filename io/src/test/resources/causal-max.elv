// Bayesian Network
//   Elvira format 

bnet  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = (present , absent);

// Variables 

node X1(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =175;
pos_y =134;
relevance = 7.0;
purpose = "";
num-states = 3;
states = ("increased" "normal" "decreased");
}

node X2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =411;
pos_y =138;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Y(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =289;
pos_y =265;
relevance = 7.0;
purpose = "";
num-states = 4;
states = ("severe" "moderate" "mild" "absent");
}

// Links of the associated graph:

link X1 Y;

link X2 Y;

//Network Relationships: 

relation X1 { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.3333333333333333 0.3333333333333333 0.3333333333333333 );
}

relation X2 { 
comment = "";
deterministic=false;
values= table (0.5 0.5 );
}

relation Y X1 X2 { 
comment = "";
deterministic=false;
values= function  
          GeneralizedMax(YX1,YX2,YResidual);

}

relation Y X1 { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = YX1;
deterministic=false;
values= table (0.41 0.0 0.01 0.32 0.0 0.08 0.18 0.0 0.24 0.09 1.0 0.67 );
}

relation Y X2 { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = YX2;
deterministic=false;
values= table (0.09 0.0 0.27 0.0 0.15 0.0 0.49 1.0 );
}

relation Y { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = YResidual;
deterministic=false;
values= table (0.0 0.0 0.0 1.0 );
}

}
