// Bayesian Network
//   Elvira format 

bnet  Unknown { 

// Network Properties

version = 1.0;
default node states = (absent , present);

// Network Variables 

node HISTORY(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =173;
pos_y =258;
relevance = 7.0;
num-states = 2;
states = (TRUE FALSE);
}

node CVP(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =76;
pos_y =274;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node PCWP(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =508;
pos_y =13;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node HYPOVOLEMIA(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =342;
pos_y =69;
relevance = 7.0;
num-states = 2;
states = (TRUE FALSE);
}

node LVEDVOLUME(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =111;
pos_y =82;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node LVFAILURE(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =283;
pos_y =13;
relevance = 7.0;
num-states = 2;
states = (TRUE FALSE);
}

node STROKEVOLUME(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =129;
pos_y =424;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node ERRLOWOUTPUT(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =30;
pos_y =387;
relevance = 7.0;
num-states = 2;
states = (TRUE FALSE);
}

node HRBP(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =19;
pos_y =331;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node HREKG(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =570;
pos_y =49;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node ERRCAUTER(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =509;
pos_y =101;
relevance = 7.0;
num-states = 2;
states = (TRUE FALSE);
}

node HRSAT(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =534;
pos_y =198;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node INSUFFANESTH(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =460;
pos_y =355;
relevance = 7.0;
num-states = 2;
states = (TRUE FALSE);
}

node ANAPHYLAXIS(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =312;
pos_y =428;
relevance = 7.0;
num-states = 2;
states = (TRUE FALSE);
}

node TPR(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =252;
pos_y =378;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node EXPCO2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =489;
pos_y =301;
relevance = 7.0;
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node KINKEDTUBE(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =349;
pos_y =358;
relevance = 7.0;
num-states = 2;
states = (TRUE FALSE);
}

node MINVOL(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =242;
pos_y =338;
relevance = 7.0;
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node FIO2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =290;
pos_y =297;
relevance = 7.0;
num-states = 2;
states = (LOW NORMAL);
}

node PVSAT(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =371;
pos_y =286;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node SAO2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =361;
pos_y =209;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node PAP(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =478;
pos_y =187;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node PULMEMBOLUS(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =350;
pos_y =189;
relevance = 7.0;
num-states = 2;
states = (TRUE FALSE);
}

node SHUNT(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =34;
pos_y =230;
relevance = 7.0;
num-states = 2;
states = (NORMAL HIGH);
}

node INTUBATION(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =240;
pos_y =220;
relevance = 7.0;
num-states = 3;
states = (NORMAL ESOPHAGEAL ONESIDED);
}

node PRESS(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =30;
pos_y =154;
relevance = 7.0;
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node DISCONNECT(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =92;
pos_y =27;
relevance = 7.0;
num-states = 2;
states = (TRUE FALSE);
}

node MINVOLSET(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =40;
pos_y =66;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node VENTMACH(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =424;
pos_y =45;
relevance = 7.0;
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node VENTTUBE(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =286;
pos_y =48;
relevance = 7.0;
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node VENTLUNG(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =265;
pos_y =104;
relevance = 7.0;
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node VENTALV(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =139;
pos_y =140;
relevance = 7.0;
num-states = 4;
states = (ZERO LOW NORMAL HIGH);
}

node ARTCO2(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =267;
pos_y =138;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node CATECHOL(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =451;
pos_y =112;
relevance = 7.0;
num-states = 2;
states = (NORMAL HIGH);
}

node HR(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =147;
pos_y =216;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node CO(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =110;
pos_y =329;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

node BP(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =201;
pos_y =432;
relevance = 7.0;
num-states = 3;
states = (LOW NORMAL HIGH);
}

// links of the associated graph:

link LVFAILURE HISTORY;

link LVEDVOLUME CVP;

link LVEDVOLUME PCWP;

link HYPOVOLEMIA LVEDVOLUME;

link LVFAILURE LVEDVOLUME;

link HYPOVOLEMIA STROKEVOLUME;

link LVFAILURE STROKEVOLUME;

link ERRLOWOUTPUT HRBP;

link HR HRBP;

link ERRCAUTER HREKG;

link HR HREKG;

link ERRCAUTER HRSAT;

link HR HRSAT;

link ANAPHYLAXIS TPR;

link ARTCO2 EXPCO2;

link VENTLUNG EXPCO2;

link INTUBATION MINVOL;

link VENTLUNG MINVOL;

link FIO2 PVSAT;

link VENTALV PVSAT;

link PVSAT SAO2;

link SHUNT SAO2;

link PULMEMBOLUS PAP;

link INTUBATION SHUNT;

link PULMEMBOLUS SHUNT;

link INTUBATION PRESS;

link KINKEDTUBE PRESS;

link VENTTUBE PRESS;

link MINVOLSET VENTMACH;

link DISCONNECT VENTTUBE;

link VENTMACH VENTTUBE;

link INTUBATION VENTLUNG;

link KINKEDTUBE VENTLUNG;

link VENTTUBE VENTLUNG;

link INTUBATION VENTALV;

link VENTLUNG VENTALV;

link VENTALV ARTCO2;

link ARTCO2 CATECHOL;

link INSUFFANESTH CATECHOL;

link SAO2 CATECHOL;

link TPR CATECHOL;

link CATECHOL HR;

link HR CO;

link STROKEVOLUME CO;

link CO BP;

link TPR BP;

//Network Relationships: 

relation HISTORY LVFAILURE { 
values= table (0.9 0.01 0.1 0.99 );
}

relation CVP LVEDVOLUME { 
values= table (0.95 0.04 0.01 0.04 0.95 0.29 0.01 0.01 0.7 );
}

relation PCWP LVEDVOLUME { 
values= table (0.95 0.04 0.01 0.04 0.95 0.04 0.01 0.01 0.95 );
}

relation HYPOVOLEMIA { 
values= table (0.2 0.8 );
}

relation LVEDVOLUME HYPOVOLEMIA LVFAILURE { 
values= table (0.95 0.01 0.98 0.05 0.04 0.09 0.01 0.9 0.01 0.9 0.01 0.05 );
}

relation LVFAILURE { 
values= table (0.05 0.95 );
}

relation STROKEVOLUME HYPOVOLEMIA LVFAILURE { 
values= table (0.98 0.5 0.95 0.05 0.01 0.49 0.04 0.9 0.01 0.01 0.01 0.05 );
}

relation ERRLOWOUTPUT { 
values= table (0.05 0.95 );
}

relation HRBP ERRLOWOUTPUT HR { 
values= table (0.98 0.3 0.01 0.4 0.98 0.01 0.01 0.4 0.98 0.59 0.01 0.01 0.01 0.3 0.01 0.01 0.01 0.98 );
}

relation HREKG ERRCAUTER HR { 
values= table (0.33333334 0.33333334 0.01 0.33333334 0.98 0.01 0.33333334 0.33333334 0.98 0.33333334 0.01 0.01 0.33333334 0.33333334 0.01 0.33333334 0.01 0.98 );
}

relation ERRCAUTER { 
values= table (0.1 0.9 );
}

relation HRSAT ERRCAUTER HR { 
values= table (0.33333334 0.33333334 0.01 0.33333334 0.98 0.01 0.33333334 0.33333334 0.98 0.33333334 0.01 0.01 0.33333334 0.33333334 0.01 0.33333334 0.01 0.98 );
}

relation INSUFFANESTH { 
values= table (0.1 0.9 );
}

relation ANAPHYLAXIS { 
values= table (0.01 0.99 );
}

relation TPR ANAPHYLAXIS { 
values= table (0.98 0.3 0.01 0.4 0.01 0.3 );
}

relation EXPCO2 ARTCO2 VENTLUNG { 
values= table (0.97 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.97 0.01 0.01 0.97 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.97 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.97 );
}

relation KINKEDTUBE { 
values= table (0.04 0.96 );
}

relation MINVOL INTUBATION VENTLUNG { 
values= table (0.97 0.01 0.5 0.01 0.01 0.97 0.5 0.01 0.01 0.6 0.97 0.01 0.01 0.01 0.48 0.97 0.97 0.01 0.48 0.01 0.01 0.38 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.97 0.97 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.97 );
}

relation FIO2 { 
values= table (0.05 0.95 );
}

relation PVSAT FIO2 VENTALV { 
values= table (1.0 0.95 1.0 0.01 0.99 0.95 0.95 0.01 0.0 0.04 0.0 0.95 0.01 0.04 0.04 0.01 0.0 0.01 0.0 0.04 0.0 0.01 0.01 0.98 );
}

relation SAO2 PVSAT SHUNT { 
values= table (0.98 0.98 0.01 0.98 0.01 0.69 0.01 0.01 0.98 0.01 0.01 0.3 0.01 0.01 0.01 0.01 0.98 0.01 );
}

relation PAP PULMEMBOLUS { 
values= table (0.01 0.05 0.19 0.9 0.8 0.05 );
}

relation PULMEMBOLUS { 
values= table (0.01 0.99 );
}

relation SHUNT INTUBATION PULMEMBOLUS { 
values= table (0.1 0.95 0.1 0.95 0.01 0.05 0.9 0.05 0.9 0.05 0.99 0.95 );
}

relation INTUBATION { 
values= table (0.92 0.03 0.05 );
}

relation PRESS INTUBATION KINKEDTUBE VENTTUBE { 
values= table (0.97 0.05 0.97 0.2 0.01 0.01 0.01 0.010000001 0.01 0.01 0.01 0.2 0.97 0.01 0.97 0.01 0.01 0.97 0.01 0.97 0.1 0.01 0.4 0.01 0.01 0.25 0.01 0.75 0.01 0.29 0.01 0.90000004 0.3 0.15 0.97 0.7 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.84 0.01 0.58 0.01 0.01 0.25 0.01 0.04 0.01 0.3 0.01 0.080000006 0.49 0.25 0.01 0.09 0.01 0.08 0.01 0.38 0.08 0.01 0.97 0.01 0.05 0.01 0.01 0.01 0.01 0.45 0.01 0.01 0.97 0.4 0.97 0.010000001 0.2 0.59 0.01 0.01 0.01 0.9 0.01 0.6 0.9 0.01 0.01 0.01 0.01 0.97 0.01 0.97 );
}

relation DISCONNECT { 
values= table (0.1 0.9 );
}

relation MINVOLSET { 
values= table (0.05 0.9 0.05 );
}

relation VENTMACH MINVOLSET { 
values= table (0.05 0.05 0.05 0.93 0.01 0.01 0.01 0.93 0.01 0.01 0.01 0.93 );
}

relation VENTTUBE DISCONNECT VENTMACH { 
values= table (0.97 0.97 0.97 0.01 0.97 0.97 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.97 );
}

relation VENTLUNG INTUBATION KINKEDTUBE VENTTUBE { 
values= table (0.97 0.97 0.97 0.97 0.3 0.95000005 0.01 0.01 0.95000005 0.97 0.01 0.97 0.97 0.5 0.97 0.01 0.4 0.97 0.01 0.97 0.97 0.3 0.97 0.01 0.01 0.01 0.01 0.01 0.68 0.030000001 0.01 0.97 0.030000001 0.01 0.97 0.01 0.01 0.48 0.01 0.01 0.58 0.01 0.01 0.01 0.01 0.68 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.010000001 0.01 0.01 0.010000001 0.01 0.01 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.010000001 0.97 0.01 0.010000001 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.01 0.97 );
}

relation VENTALV INTUBATION VENTLUNG { 
values= table (0.97 0.01 0.01 0.030000001 0.01 0.97 0.01 0.01 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.95000005 0.97 0.01 0.01 0.94 0.01 0.97 0.01 0.88 0.01 0.01 0.97 0.010000001 0.01 0.01 0.01 0.04 0.97 0.01 0.01 0.1 0.01 0.97 0.01 0.010000001 0.01 0.01 0.97 0.01 0.01 0.01 0.01 0.01 );
}

relation ARTCO2 VENTALV { 
values= table (0.01 0.01 0.04 0.9 0.01 0.01 0.92 0.09 0.98 0.98 0.04 0.01 );
}

relation CATECHOL ARTCO2 INSUFFANESTH SAO2 TPR { 
values= table (0.01 0.01 0.7 0.01 0.05 0.7 0.01 0.05 0.95 0.01 0.05 0.7 0.01 0.05 0.95 0.05 0.05 0.95 0.01 0.01 0.7 0.01 0.05 0.7 0.01 0.05 0.99 0.01 0.05 0.7 0.01 0.05 0.99 0.05 0.05 0.99 0.01 0.01 0.1 0.01 0.01 0.1 0.01 0.01 0.3 0.01 0.01 0.1 0.01 0.01 0.3 0.01 0.01 0.3 0.99 0.99 0.3 0.99 0.95 0.3 0.99 0.95 0.05 0.99 0.95 0.3 0.99 0.95 0.05 0.95 0.95 0.05 0.99 0.99 0.3 0.99 0.95 0.3 0.99 0.95 0.01 0.99 0.95 0.3 0.99 0.95 0.01 0.95 0.95 0.01 0.99 0.99 0.9 0.99 0.99 0.9 0.99 0.99 0.7 0.99 0.99 0.9 0.99 0.99 0.7 0.99 0.99 0.7 );
}

relation HR CATECHOL { 
values= table (0.05 0.01 0.9 0.09 0.05 0.9 );
}

relation CO HR STROKEVOLUME { 
values= table (0.98 0.95 0.3 0.95 0.04 0.01 0.8 0.01 0.01 0.01 0.04 0.69 0.04 0.95 0.3 0.19 0.04 0.01 0.01 0.01 0.01 0.01 0.01 0.69 0.01 0.95 0.98 );
}

relation BP CO TPR { 
values= table (0.98 0.98 0.3 0.98 0.1 0.05 0.9 0.05 0.01 0.01 0.01 0.6 0.01 0.85 0.4 0.09 0.2 0.09 0.01 0.01 0.1 0.01 0.05 0.55 0.01 0.75 0.9 );
}

}
