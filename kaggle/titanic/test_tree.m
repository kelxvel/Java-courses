function [numNode, errors] = test_tree(T, xTest, yTest)
    maxPruneLevel = max(T.prunelist) - 1;
    for i = 1:maxPruneLevel
        pruneT = prune(T, 'level', i);
        yFit = eval(pruneT, xTest);
        if iscell(yFit)
            yFit = cell2mat(yFit);
            yFit = str2num(yFit);
        end
        e = yFit ~= yTest;
        numNode(i) = numnodes(pruneT);
        errors(i) = sum(e);
    end
end

