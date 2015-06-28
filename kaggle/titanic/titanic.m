fid = fopen('train.csv');
train = textscan(fid, '%d%d%d%q%s%d%d%d%s%f%s%s', 'delimiter', ',', ...
    'headerLines', 1);
fclose(fid);

varNames = { 'PassengerId', 'Survived', 'Pclass', 'Name', 'Sex',...
    'Age', 'SibSp', 'Parch', 'Ticket', 'Fare', 'Cabin', 'Embarked' };
xNames = { 'Pclass', 'Sex', 'Age', 'SibSp', 'Parch', 'Fare' };
yNames = { 'Survived' };

trainIdx = 1:2:length(train{1});
testIdx = 2:2:length(train{1});

trainSet = dataset(train{:});
trainSet.Properties.VarNames = varNames;
trainSet.Sex = grp2idx(cellstr(trainSet.Sex));

xTrain = double(trainSet(trainIdx, xNames));
yTrain = double(trainSet(trainIdx, yNames));

xTest = double(trainSet(testIdx, xNames));
yTest = double(trainSet(testIdx, yNames));

T = classregtree(xTrain, yTrain, 'method', 'classification', ... 
    'splitcriterion', 'twoing', 'categorical', [1 2], 'names', xNames );

[numNodes, errors] = test_tree(T, xTest, yTest);
[minErrors, minIdx] = min(errors);
optimalTree = prune(T, 'level', minIdx);

%------------------
fid = fopen('test.csv');
test = textscan(fid, '%d%d%q%s%d%d%d%s%f%s%s', 'delimiter', ',', ...
    'headerLines', 1);
fclose(fid);

ctrlSet = dataset(test{:});
ctrlSet.Properties.VarNames = { 'PassengerId', 'Pclass', 'Name', 'Sex',...
    'Age', 'SibSp', 'Parch', 'Ticket', 'Fare', 'Cabin', 'Embarked' };
ctrlSet.Sex = grp2idx(cellstr(ctrlSet.Sex));

xCtrl = double(ctrlSet(:, xNames));
yCtrl = eval(optimalTree, xCtrl);
yCtrl = str2num(cell2mat(yCtrl));

fname = 'ttr1.csv';
fid = fopen(fname, 'w');
fprintf(fid, 'PassengerId,Survived\n');
fclose(fid);
dlmwrite(fname, [ctrlSet.PassengerId yCtrl], '-append', 'delimiter', ',');