% Removes rows corresponding to failures that
% match the given names.

function f2 = restrict_failures_byname(f,fnames,names)
maxsize = 50; % fixed.
assert(size(f,2)==maxsize);
assert(size(fnames,1)==1);
assert(size(f,1)==size(fnames,2));

matches=ismember(fnames,names);
f2 = zeros(1,maxsize);

for i=1:size(matches,2)
    if (matches(i))
        f2 = f2 + f(i,:);
    end
end

assert(size(f2,1)==1 && size(f2,2)==50);

