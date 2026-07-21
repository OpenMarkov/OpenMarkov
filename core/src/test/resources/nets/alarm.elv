// Bayesian Network
//   Elvira format 

bnet  "Unknown" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00000000";
version = 1.0;
default node states = (absent , present);

// Variables 

node HISTORY(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =429;
pos_y =533;
relevance = 7.0;
purpose = "";
num-states = 2;
states = ("TRUE" "FALSE");
}

node CVP(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1075;
pos_y =425;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node PCWP(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1046;
pos_y =577;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node HYPOVOLEMIA(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =351;
pos_y =65;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (TRUE FALSE);
}

node LVEDVOLUME(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =919;
pos_y =242;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node LVFAILURE(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =784;
pos_y =60;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (TRUE FALSE);
}

node STROKEVOLUME(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =719;
pos_y =746;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node ERRLOWOUTPUT(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =154;
pos_y =840;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (TRUE FALSE);
}

node HRBP(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =183;
pos_y =952;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node HREKG(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1470;
pos_y =703;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node ERRCAUTER(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1460;
pos_y =582;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (TRUE FALSE);
}

node HRSAT(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1346;
pos_y =971;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node INSUFFANESTH(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1260;
pos_y =54;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (TRUE FALSE);
}

node ANAPHYLAXIS(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1444;
pos_y =59;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (TRUE FALSE);
}

node TPR(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1289;
pos_y =246;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node EXPCO2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1468;
pos_y =839;
relevance = 7.0;
purpose = "";
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node KINKEDTUBE(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =322;
pos_y =269;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (TRUE FALSE);
}

node MINVOL(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =869;
pos_y =672;
relevance = 7.0;
purpose = "";
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node FIO2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =999;
pos_y =60;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (LOW NORMAL);
}

node PVSAT(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1089;
pos_y =300;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node SAO2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1304;
pos_y =468;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node PAP(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =570;
pos_y =349;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node PULMEMBOLUS(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =564;
pos_y =63;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (TRUE FALSE);
}

node SHUNT(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =115;
pos_y =544;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (NORMAL HIGH);
}

node INTUBATION(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =357;
pos_y =361;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (NORMAL ESOPHAGEAL ONESIDED);
}

node PRESS(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =229;
pos_y =664;
relevance = 7.0;
purpose = "";
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node DISCONNECT(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1127;
pos_y =126;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (TRUE FALSE);
}

node MINVOLSET(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =120;
pos_y =58;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node VENTMACH(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =166;
pos_y =181;
relevance = 7.0;
purpose = "";
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node VENTTUBE(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =710;
pos_y =272;
relevance = 7.0;
purpose = "";
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node VENTLUNG(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =743;
pos_y =455;
relevance = 7.0;
purpose = "";
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node VENTALV(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =587;
pos_y =643;
relevance = 7.0;
purpose = "";
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node ARTCO2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =372;
pos_y =753;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node CATECHOL(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1124;
pos_y =725;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (NORMAL HIGH);
}

node HR(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1080;
pos_y =838;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node CO(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =752;
pos_y =879;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

node BP(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1121;
pos_y =972;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (LOW NORMAL HIGH);
}

// Links of the associated graph:

link ANAPHYLAXIS TPR;

link ARTCO2 CATECHOL;

link ARTCO2 EXPCO2;

link CATECHOL HR;

link CO BP;

link DISCONNECT VENTTUBE;

link ERRCAUTER HREKG;

link ERRCAUTER HRSAT;

link ERRLOWOUTPUT HRBP;

link FIO2 PVSAT;

link HR CO;

link HR HRBP;

link HR HREKG;

link HR HRSAT;

link HYPOVOLEMIA LVEDVOLUME;

link HYPOVOLEMIA STROKEVOLUME;

link INSUFFANESTH CATECHOL;

link INTUBATION MINVOL;

link INTUBATION PRESS;

link INTUBATION SHUNT;

link INTUBATION VENTALV;

link INTUBATION VENTLUNG;

link KINKEDTUBE PRESS;

link KINKEDTUBE VENTLUNG;

link LVEDVOLUME CVP;

link LVEDVOLUME PCWP;

link LVFAILURE HISTORY;

link LVFAILURE LVEDVOLUME;

link LVFAILURE STROKEVOLUME;

link MINVOLSET VENTMACH;

link PULMEMBOLUS PAP;

link PULMEMBOLUS SHUNT;

link PVSAT SAO2;

link SAO2 CATECHOL;

link SHUNT SAO2;

link STROKEVOLUME CO;

link TPR BP;

link TPR CATECHOL;

link VENTALV ARTCO2;

link VENTALV PVSAT;

link VENTLUNG EXPCO2;

link VENTLUNG MINVOL;

link VENTLUNG VENTALV;

link VENTMACH VENTTUBE;

link VENTTUBE PRESS;

link VENTTUBE VENTLUNG;

//Network Relationships: 

relation HISTORY LVFAILURE { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.9 0.01 0.1 0.99 );
}

relation CVP LVEDVOLUME { 
comment = "";
deterministic=false;
values= table (0.95 0.04 0.01 0.04 0.95 0.29 0.01 0.01 0.7 );
}

relation PCWP LVEDVOLUME { 
comment = "";
deterministic=false;
values= table (0.95 0.04 0.01 0.04 0.95 0.04 0.01 0.01 0.95 );
}

relation HYPOVOLEMIA { 
comment = "";
deterministic=false;
values= table (0.2 0.8 );
}

relation LVEDVOLUME HYPOVOLEMIA LVFAILURE { 
comment = "";
kind-of-relation = potential;
deterministic=false;
values= table (0.95 0.01 0.98 0.05 0.04 0.09 0.01 0.9 0.01 0.9 0.01 0.05 );
}

relation LVFAILURE { 
comment = "";
deterministic=false;
values= table (0.05 0.95 );
}

relation STROKEVOLUME HYPOVOLEMIA LVFAILURE { 
comment = "";
deterministic=false;
values= table (0.98 0.5 0.95 0.05 0.01 0.49 0.04 0.9 0.01 0.01 0.01 0.05 );
}

relation ERRLOWOUTPUT { 
comment = "";
deterministic=false;
values= table (0.05 0.95 );
}

relation HRBP ERRLOWOUTPUT HR { 
comment = "";
deterministic=false;
values= table (0.98 0.3 0.01 0.4 0.98 0.01 0.01 0.4 0.98 0.59 0.01 0.01 0.01 0.3 0.01 0.01 0.01 0.98 );
}

relation HREKG ERRCAUTER HR { 
comment = "";
deterministic=false;
values= table (0.33333334 0.33333334 0.01 0.33333334 0.98 0.01 0.33333334 0.33333334 0.98 0.33333334 0.01 0.01 0.33333334 0.33333334 0.01 0.33333334 0.01 0.98 );
}

relation ERRCAUTER { 
comment = "";
deterministic=false;
values= table (0.1 0.9 );
}

relation HRSAT ERRCAUTER HR { 
comment = "";
deterministic=false;
values= table (0.33333334 0.33333334 0.01 0.33333334 0.98 0.01 0.33333334 0.33333334 0.98 0.33333334 0.01 0.01 0.33333334 0.33333334 0.01 0.33333334 0.01 0.98 );
}

relation INSUFFANESTH { 
comment = "";
deterministic=false;
values= table (0.1 0.9 );
}

relation ANAPHYLAXIS { 
comment = "";
deterministic=false;
values= table (0.01 0.99 );
}

relation TPR ANAPHYLAXIS { 
comment = "";
deterministic=false;
values= table (0.98 0.3 0.01 0.4 0.01 0.3 );
}

relation EXPCO2 ARTCO2 VENTLUNG { 
comment = "";
deterministic=false;
values= table (0.97 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.97 0.01 0.01 0.97 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.97 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.97 );
}

relation KINKEDTUBE { 
comment = "";
deterministic=false;
values= table (0.04 0.96 );
}

relation MINVOL INTUBATION VENTLUNG { 
comment = "";
deterministic=false;
values= table (0.97 0.01 0.5 0.01 0.01 0.97 0.5 0.01 0.01 0.6 0.97 0.01 0.01 0.01 0.48 0.97 0.97 0.01 0.48 0.01 0.01 0.38 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.97 0.97 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.97 );
}

relation FIO2 { 
comment = "";
deterministic=false;
values= table (0.05 0.95 );
}

relation PVSAT FIO2 VENTALV { 
comment = "";
deterministic=false;
values= table (1.0 0.95 1.0 0.01 0.99 0.95 0.95 0.01 0.0 0.04 0.0 0.95 0.01 0.04 0.04 0.01 0.0 0.01 0.0 0.04 0.0 0.01 0.01 0.98 );
}

relation SAO2 PVSAT SHUNT { 
comment = "";
deterministic=false;
values= table (0.98 0.98 0.01 0.98 0.01 0.69 0.01 0.01 0.98 0.01 0.01 0.3 0.01 0.01 0.01 0.01 0.98 0.01 );
}

relation PAP PULMEMBOLUS { 
comment = "";
deterministic=false;
values= table (0.01 0.05 0.19 0.9 0.8 0.05 );
}

relation PULMEMBOLUS { 
comment = "";
deterministic=false;
values= table (0.01 0.99 );
}

relation SHUNT INTUBATION PULMEMBOLUS { 
comment = "";
deterministic=false;
values= table (0.1 0.95 0.1 0.95 0.01 0.05 0.9 0.05 0.9 0.05 0.99 0.95 );
}

relation INTUBATION { 
comment = "";
deterministic=false;
values= table (0.92 0.03 0.05 );
}

relation PRESS INTUBATION KINKEDTUBE VENTTUBE { 
comment = "";
deterministic=false;
values= table (0.97 0.05 0.97 0.2 0.01 0.01 0.01 0.010000001 0.01 0.01 0.01 0.2 0.97 0.01 0.97 0.01 0.01 0.97 0.01 0.97 0.1 0.01 0.4 0.01 0.01 0.25 0.01 0.75 0.01 0.29 0.01 0.90000004 0.3 0.15 0.97 0.7 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.84 0.01 0.58 0.01 0.01 0.25 0.01 0.04 0.01 0.3 0.01 0.080000006 0.49 0.25 0.01 0.09 0.01 0.08 0.01 0.38 0.08 0.01 0.97 0.01 0.05 0.01 0.01 0.01 0.01 0.45 0.01 0.01 0.97 0.4 0.97 0.010000001 0.2 0.59 0.01 0.01 0.01 0.9 0.01 0.6 0.9 0.01 0.01 0.01 0.01 0.97 0.01 0.97 );
}

relation DISCONNECT { 
comment = "";
deterministic=false;
values= table (0.1 0.9 );
}

relation MINVOLSET { 
comment = "";
deterministic=false;
values= table (0.05 0.9 0.05 );
}

relation VENTMACH MINVOLSET { 
comment = "";
deterministic=false;
values= table (0.05 0.05 0.05 0.93 0.01 0.01 0.01 0.93 0.01 0.01 0.01 0.93 );
}

relation VENTTUBE DISCONNECT VENTMACH { 
comment = "";
deterministic=false;
values= table (0.97 0.97 0.97 0.01 0.97 0.97 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.97 );
}

relation VENTLUNG INTUBATION KINKEDTUBE VENTTUBE { 
comment = "";
deterministic=false;
values= table (0.97 0.97 0.97 0.97 0.3 0.95000005 0.01 0.01 0.95000005 0.97 0.01 0.97 0.97 0.5 0.97 0.01 0.4 0.97 0.01 0.97 0.97 0.3 0.97 0.01 0.01 0.01 0.01 0.01 0.68 0.030000001 0.01 0.97 0.030000001 0.01 0.97 0.01 0.01 0.48 0.01 0.01 0.58 0.01 0.01 0.01 0.01 0.68 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.010000001 0.01 0.01 0.010000001 0.01 0.01 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.010000001 0.97 0.01 0.010000001 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.97 );
}

relation VENTALV INTUBATION VENTLUNG { 
comment = "";
deterministic=false;
values= table (0.97 0.01 0.01 0.030000001 0.01 0.97 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.95000005 0.97 0.01 0.01 0.94 0.01 0.97 0.01 0.88 0.01 0.01 0.97 0.010000001 0.01 0.01 0.01 0.04 0.97 0.01 0.01 0.1 0.01 0.97 0.01 0.010000001 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.01 );
}

relation ARTCO2 VENTALV { 
comment = "";
deterministic=false;
values= table (0.01 0.01 0.04 0.9 0.01 0.01 0.92 0.09 0.98 0.98 0.04 0.01 );
}

relation CATECHOL ARTCO2 INSUFFANESTH SAO2 TPR { 
comment = "";
deterministic=false;
values= table (0.01 0.01 0.7 0.01 0.05 0.7 0.01 0.05 0.95 0.01 0.05 0.7 0.01 0.05 0.95 0.05 0.05 0.95 0.01 0.01 0.7 0.01 0.05 0.7 0.01 0.05 0.99 0.01 0.05 0.7 0.01 0.05 0.99 0.05 0.05 0.99 0.01 0.01 0.1 0.01 0.01 0.1 0.01 0.01 0.3 0.01 0.01 0.1 0.01 0.01 0.3 0.01 0.01 0.3 0.99 0.99 0.3 0.99 0.95 0.3 0.99 0.95 0.05 0.99 0.95 0.3 0.99 0.95 0.05 0.95 0.95 0.05 0.99 0.99 0.3 0.99 0.95 0.3 0.99 0.95 0.01 0.99 0.95 0.3 0.99 0.95 0.01 0.95 0.95 0.01 0.99 0.99 0.9 0.99 0.99 0.9 0.99 0.99 0.7 0.99 0.99 0.9 0.99 0.99 0.7 0.99 0.99 0.7 );
}

relation HR CATECHOL { 
comment = "";
deterministic=false;
values= table (0.05 0.01 0.9 0.09 0.05 0.9 );
}

relation CO HR STROKEVOLUME { 
comment = "";
deterministic=false;
values= table (0.98 0.95 0.3 0.95 0.04 0.01 0.8 0.01 0.01 0.01 0.04 0.69 0.04 0.95 0.3 0.19 0.04 0.01 0.01 0.01 0.01 0.01 0.01 0.69 0.01 0.95 0.98 );
}

relation BP CO TPR { 
comment = "";
deterministic=false;
values= table (0.98 0.98 0.3 0.98 0.1 0.05 0.9 0.05 0.01 0.01 0.01 0.6 0.01 0.85 0.4 0.09 0.2 0.09 0.01 0.01 0.1 0.01 0.05 0.55 0.01 0.75 0.9 );
}

}
