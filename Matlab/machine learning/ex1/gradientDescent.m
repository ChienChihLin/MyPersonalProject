function [theta, J_history] = gradientDescent(X, y, theta, alpha, num_iters)
%GRADIENTDESCENT Performs gradient descent to learn theta
%   theta = GRADIENTDESCENT(X, y, theta, alpha, num_iters) updates theta by 
%   taking num_iters gradient steps with learning rate alpha

% Initialize some useful values
m = length(y); % number of training examples
J_history = zeros(num_iters, 1);
[column, row] = size(theta);
tmp_theta = zeros(column,row);

for iter = 1:num_iters

    % ====================== YOUR CODE HERE ======================
    % Instructions: Perform a single gradient step on the parameter vector
    %               theta. 
    %
    % Hint: While debugging, it can be useful to print out the values
    %       of the cost function (computeCost) and gradient here.
    %
	for num = 1:column
		tmp_theta(num,1) = theta(num,1) - (alpha * (sum((((theta'*X')' - y).*X(:,num)))/m));
	end

	%tmp_theta(1,1) = theta(1,1) - (alpha * (sum(((theta'*X')' - y))/m));
	%tmp_theta(2,1) = theta(2,1) - (alpha * (sum((((theta'*X')' - y).*X(:,2)))/m));
	theta = tmp_theta;





    % ============================================================

    % Save the cost J in every iteration    
    J_history(iter) = computeCost(X, y, theta);

end

end
