To clone all OpenMarkov to your computer:

1. Copy and paste the next lines in your git terminal of the desired folder (*git Bash* in windows and the default *shell/terminal* in Linux).
2. Press *Enter*. All the lines **except the last one** will run automatically as the terminal sees a line-break as the order "run that command".
3. Because of this, when the download ends, you should press *Enter* again to "run" the last command or you will miss the last repository.

To clone only some of them, copy only those lines but don't forget to keep `org.openmarkov` and `org.openmarkov.full`:  

```
git clone -b development https://bitbucket.org/cisiad/org.openmarkov
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.annotationProcessing
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.core

git clone -b development https://bitbucket.org/cisiad/org.openmarkov.full  

git clone -b development https://bitbucket.org/cisiad/org.openmarkov.bnEvaluation
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.costEffectiveness
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.dbGenerator
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.gui
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.inference
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.integrationTests
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.io
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.io.database.elvira
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.io.database.excel
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.io.database.weka
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.learning.algorithm
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.learning.core
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.learning.gui
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.learning.metric
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.stochasticPropagationOutput
git clone -b development https://bitbucket.org/cisiad/org.openmarkov.sensitivityAnalysis
```