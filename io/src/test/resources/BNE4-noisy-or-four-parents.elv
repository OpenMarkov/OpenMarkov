// Bayesian Network
//   Elvira format 

bnet  "Untitled1" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = (present , absent);

// Variables 

node Paludism(finite-states) {
title = "Paludism";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =107;
pos_y =94;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Pneumonia(finite-states) {
title = "Pneumonia";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =306;
pos_y =94;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Meningitis(finite-states) {
title = "Meningitis";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =504;
pos_y =92;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Flu(finite-states) {
title = "Flu";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =711;
pos_y =91;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

node Fever(finite-states) {
title = "Fever";
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =372;
pos_y =206;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("present" "absent");
}

// Links of the associated graph:

link Flu Fever;

link Meningitis Fever;

link Paludism Fever;

link Pneumonia Fever;

//Network Relationships: 

relation Paludism { 
comment = "";
deterministic=false;
values= table (0.5 0.5 );
}

relation Pneumonia { 
comment = "";
deterministic=false;
values= table (0.5 0.5 );
}

relation Meningitis { 
comment = "";
deterministic=false;
values= table (0.5 0.5 );
}

relation Flu { 
comment = "";
deterministic=false;
values= table (0.5 0.5 );
}

relation Fever Paludism Pneumonia Meningitis Flu { 
comment = "";
deterministic=false;
values= function  
          Or(FeverPaludism,FeverPneumonia,FeverMeningitis,FeverFlu,FeverResidual);

henrionVSdiez = "Diez";
}

relation Fever Paludism { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = FeverPaludism;
deterministic=false;
values= table (0.9 0.0 0.1 1.0 );
}

relation Fever Pneumonia { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = FeverPneumonia;
deterministic=false;
values= table (0.93 0.0 0.07 1.0 );
}

relation Fever Meningitis { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = FeverMeningitis;
deterministic=false;
values= table (0.95 0.0 0.05 1.0 );
}

relation Fever Flu { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = FeverFlu;
deterministic=false;
values= table (0.87 0.0 0.13 1.0 );
}

relation Fever { 
comment = "new";
kind-of-relation = potential;
active=false;
name-of-relation = FeverResidual;
deterministic=false;
values= table (1.0E-4 0.9999 );
}

}
