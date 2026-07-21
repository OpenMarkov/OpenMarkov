// Influence Diagram With Super Value Nodes
//   Elvira format 

id-with-svnodes  "" { 

// Network Properties

kindofgraph = "directed";
visualprecision = "0.00";
version = 1.0;
default node states = ("present" , "absent");

// Variables 

node N2_N3(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =50;
pos_y =27;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (positive negative);
}

node CT_scan(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =58;
pos_y =103;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (positive negative);
}

node TBNA(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =291;
pos_y =181;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (positive negative no_result);
}

node PET(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =610;
pos_y =143;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (positive negative no_result);
}

node EBUS(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =826;
pos_y =56;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (positive negative no_result);
}

node EUS(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =873;
pos_y =270;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (positive negative no_result);
}

node MED(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1094;
pos_y =193;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (positive negative no_result);
}

node MED_Sv(finite-states) {
kind-of-node = chance;
type-of-variable = finite-states;
pos_x =1194;
pos_y =194;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node Decision_TBNA(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =130;
pos_y =178;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node Decision_PET(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =472;
pos_y =175;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node Decision_MED(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =1135;
pos_y =58;
relevance = 7.0;
purpose = "";
num-states = 2;
states = (yes no);
}

node Decision_EBUS_EUS(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =869;
pos_y =137;
relevance = 7.0;
purpose = "";
num-states = 4;
states = (ebus_eus ebus eus no_test);
}

node Treatment(finite-states) {
kind-of-node = decision;
type-of-variable = finite-states;
pos_x =1031;
pos_y =246;
relevance = 7.0;
purpose = "";
num-states = 3;
states = (thoracotomy chemotherapy palliative);
}

node Survivors_QALE(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =929;
pos_y =375;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Inmediate_Survival(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =1024;
pos_y =316;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node MED_Survival(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =1112;
pos_y =374;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Net_QALE(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =1045;
pos_y =463;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node TBNA_Morbidity(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =737;
pos_y =472;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node MED_Morbidity(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =1073;
pos_y =576;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node EUS_Morbidity(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =894;
pos_y =475;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node EBUS_Morbidity(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =763;
pos_y =385;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Total_QALE(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =868;
pos_y =644;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Economic_Cost_CT_scan(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =249;
pos_y =285;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Economic_Cost_TBNA(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =128;
pos_y =354;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Economic_Cost_EBUS(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =508;
pos_y =510;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Economic_Cost_EUS(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =329;
pos_y =464;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Economic_Cost_MED(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =358;
pos_y =346;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Economic_Cost_PET(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =492;
pos_y =431;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Economic_Cost_Treatment(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =121;
pos_y =464;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Total_Economic_Cost(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =228;
pos_y =596;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node C2E(continuous) {
kind-of-node = utility;
type-of-variable = continuous;
pos_x =607;
pos_y =577;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Weighted_Economic_Cost(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =407;
pos_y =672;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

node Net_Effectiveness(continuous) {
kind-of-node = super-value;
type-of-variable = continuous;
pos_x =623;
pos_y =771;
relevance = 7.0;
purpose = "";
min = 0;
max = 1;
precision = 2;
}

// Links of the associated graph:

link C2E Weighted_Economic_Cost;

link CT_scan Decision_TBNA;

link CT_scan EBUS;

link CT_scan EUS;

link CT_scan MED;

link CT_scan PET;

link CT_scan TBNA;

link Decision_EBUS_EUS EBUS;

link Decision_EBUS_EUS EBUS_Morbidity;

link Decision_EBUS_EUS EUS;

link Decision_EBUS_EUS EUS_Morbidity;

link Decision_EBUS_EUS Economic_Cost_EBUS;

link Decision_EBUS_EUS Economic_Cost_EUS;

link Decision_MED Economic_Cost_MED;

link Decision_MED MED;

link Decision_MED MED_Sv;

link Decision_PET Economic_Cost_PET;

link Decision_PET PET;

link Decision_TBNA Economic_Cost_TBNA;

link Decision_TBNA TBNA;

link Decision_TBNA TBNA_Morbidity;

link EBUS Decision_MED;

link EBUS_Morbidity Total_QALE;

link EUS Decision_MED;

link EUS_Morbidity Total_QALE;

link Economic_Cost_CT_scan Total_Economic_Cost;

link Economic_Cost_EBUS Total_Economic_Cost;

link Economic_Cost_EUS Total_Economic_Cost;

link Economic_Cost_MED Total_Economic_Cost;

link Economic_Cost_PET Total_Economic_Cost;

link Economic_Cost_TBNA Total_Economic_Cost;

link Economic_Cost_Treatment Total_Economic_Cost;

link Inmediate_Survival Net_QALE;

link MED Treatment;

link MED_Morbidity Total_QALE;

link MED_Survival Net_QALE;

link MED_Sv MED_Morbidity;

link MED_Sv MED_Survival;

link MED_Sv Treatment;

link N2_N3 CT_scan;

link N2_N3 EBUS;

link N2_N3 EUS;

link N2_N3 MED;

link N2_N3 PET;

link N2_N3 Survivors_QALE;

link N2_N3 TBNA;

link Net_QALE Total_QALE;

link PET Decision_EBUS_EUS;

link PET EBUS;

link PET EUS;

link PET MED;

link Survivors_QALE Net_QALE;

link TBNA Decision_PET;

link TBNA_Morbidity Total_QALE;

link Total_Economic_Cost Weighted_Economic_Cost;

link Total_QALE Net_Effectiveness;

link Treatment Economic_Cost_Treatment;

link Treatment Inmediate_Survival;

link Treatment Survivors_QALE;

link Weighted_Economic_Cost Net_Effectiveness;

//Network Relationships: 

relation N2_N3 { 
comment = "";
deterministic=false;
values= table (0.2807017543859649 0.7192982456140351 );
}

relation Economic_Cost_CT_scan { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (670.0 );
}

relation C2E { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (-3.3333333333333335E-5 );
}

relation CT_scan N2_N3 { 
comment = "";
deterministic=false;
values= table (0.5103448275862069 0.14324324324324322 0.4896551724137931 0.8567567567567568 );
}

relation TBNA CT_scan Decision_TBNA N2_N3 { 
comment = "";
deterministic=false;
values= table (0.4596774193548387 0.09565217391304348 0.0 0.0 0.02 0.07857142857142863 0.0 0.0 0.5403225806451613 0.9043478260869565 0.0 0.0 0.98 0.9214285714285714 0.0 0.0 0.0 0.0 1.0 1.0 0.0 0.0 1.0 1.0 );
}

relation PET CT_scan Decision_PET N2_N3 { 
comment = "";
deterministic=false;
values= table (0.9047619047619048 0.22499999999999998 0.0 0.0 0.7402597402597403 0.07526881720430112 0.0 0.0 0.09523809523809523 0.775 0.0 0.0 0.2597402597402597 0.9247311827956989 0.0 0.0 0.0 0.0 1.0 1.0 0.0 0.0 1.0 1.0 );
}

relation EBUS CT_scan Decision_EBUS_EUS N2_N3 PET { 
comment = "";
deterministic=false;
values= table (0.8787878787878788 0.8888888888888888 0.918918918918919 0.03448275862068961 0.024390243902439046 0.02631578947368418 0.8787878787878788 0.8888888888888888 0.918918918918919 0.03448275862068961 0.024390243902439046 0.02631578947368418 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.8918918918918919 0.8809523809523809 0.8918918918918919 0.033333333333333326 0.025000000000000022 0.022222222222222254 0.8918918918918919 0.8809523809523809 0.8918918918918919 0.033333333333333326 0.025000000000000022 0.022222222222222254 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.12121212121212122 0.11111111111111116 0.08108108108108103 0.9655172413793104 0.975609756097561 0.9736842105263158 0.12121212121212122 0.11111111111111116 0.08108108108108103 0.9655172413793104 0.975609756097561 0.9736842105263158 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.10810810810810811 0.11904761904761907 0.10810810810810811 0.9666666666666667 0.975 0.9777777777777777 0.10810810810810811 0.11904761904761907 0.10810810810810811 0.9666666666666667 0.975 0.9777777777777777 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 );
}

relation EUS CT_scan Decision_EBUS_EUS N2_N3 PET { 
comment = "";
deterministic=false;
values= table (0.8611111111111112 0.868421052631579 0.8571428571428571 0.06451612903225812 0.06666666666666665 0.0714285714285714 0.0 0.0 0.0 0.0 0.0 0.0 0.8611111111111112 0.868421052631579 0.8571428571428571 0.06451612903225812 0.06666666666666665 0.0714285714285714 0.0 0.0 0.0 0.0 0.0 0.0 0.5806451612903226 0.5666666666666667 0.7619047619047619 0.07407407407407407 0.0625 0.07692307692307687 0.0 0.0 0.0 0.0 0.0 0.0 0.5806451612903226 0.5666666666666667 0.7619047619047619 0.07407407407407407 0.0625 0.07692307692307687 0.0 0.0 0.0 0.0 0.0 0.0 0.13888888888888884 0.13157894736842102 0.1428571428571429 0.9354838709677419 0.9333333333333333 0.9285714285714286 0.0 0.0 0.0 0.0 0.0 0.0 0.13888888888888884 0.13157894736842102 0.1428571428571429 0.9354838709677419 0.9333333333333333 0.9285714285714286 0.0 0.0 0.0 0.0 0.0 0.0 0.4193548387096774 0.43333333333333335 0.23809523809523814 0.9259259259259259 0.9375 0.9230769230769231 0.0 0.0 0.0 0.0 0.0 0.0 0.4193548387096774 0.43333333333333335 0.23809523809523814 0.9259259259259259 0.9375 0.9230769230769231 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 );
}

relation MED CT_scan Decision_MED N2_N3 PET { 
comment = "";
deterministic=false;
values= table (0.8 0.8125 0.8125 0.050000000000000044 0.05882352941176472 0.0714285714285714 0.0 0.0 0.0 0.0 0.0 0.0 0.7857142857142857 0.8 0.7272727272727273 0.052631578947368474 0.0625 0.05555555555555558 0.0 0.0 0.0 0.0 0.0 0.0 0.19999999999999996 0.1875 0.1875 0.95 0.9411764705882353 0.9285714285714286 0.0 0.0 0.0 0.0 0.0 0.0 0.2142857142857143 0.19999999999999996 0.2727272727272727 0.9473684210526315 0.9375 0.9444444444444444 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 0.0 0.0 0.0 0.0 0.0 0.0 1.0 1.0 1.0 1.0 1.0 1.0 );
}

relation MED_Sv Decision_MED { 
comment = "";
deterministic=false;
values= table (0.9629629629629629 1.0 0.03703703703703709 0.0 );
}

relation Survivors_QALE N2_N3 Treatment { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (0.66 0.83 0.5 3.0 2.0 1.25 );
}

relation Inmediate_Survival Treatment { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (0.9090909090909091 0.9803921568627451 0.9811320754716981 );
}

relation MED_Survival MED_Sv { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (1.0 0.0 );
}

relation Net_QALE Inmediate_Survival MED_Survival Survivors_QALE { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Product();}

relation TBNA_Morbidity Decision_TBNA { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (-1.0E-4 0.0 );
}

relation EBUS_Morbidity Decision_EBUS_EUS { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (-0.03 -0.03 0.0 0.0 );
}

relation EUS_Morbidity Decision_EBUS_EUS { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (-0.03 0.0 -0.03 0.0 );
}

relation MED_Morbidity MED_Sv { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (-0.05 0.0 );
}

relation Total_QALE EBUS_Morbidity EUS_Morbidity MED_Morbidity Net_QALE TBNA_Morbidity { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Sum();}

relation Economic_Cost_TBNA Decision_TBNA { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (80.0 0.0 );
}

relation Economic_Cost_EBUS Decision_EBUS_EUS { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (620.0 620.0 0.0 0.0 );
}

relation Economic_Cost_EUS Decision_EBUS_EUS { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (620.0 0.0 620.0 0.0 );
}

relation Economic_Cost_MED Decision_MED { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (1620.0 0.0 );
}

relation Economic_Cost_PET Decision_PET { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (2250.0 0.0 );
}

relation Economic_Cost_Treatment Treatment { 
comment = "";
kind-of-relation = utility;
deterministic=false;
values= table (19646.0 11242.0 3000.0 );
}

relation Total_Economic_Cost Economic_Cost_CT_scan Economic_Cost_EBUS Economic_Cost_EUS Economic_Cost_MED Economic_Cost_PET Economic_Cost_TBNA Economic_Cost_Treatment { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Sum();}

relation Weighted_Economic_Cost C2E Total_Economic_Cost { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Product();}

relation Net_Effectiveness Total_QALE Weighted_Economic_Cost { 
comment = "";
kind-of-relation = utility-combination;
deterministic=false;
values= function  
          Sum();}

}
