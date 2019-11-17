/**
 * Definition for a binary tree node.
 * struct TreeNode {
 *     int val;
 *     struct TreeNode *left;
 *     struct TreeNode *right;
 * };
 */


int maxDepth(struct TreeNode* root){
    int lDepth, rDepth;
    
    if (root == NULL) {
        return 0;
    } else {
        lDepth = maxDepth(root->left);
        rDepth = maxDepth(root->right);
    }
    
    if (lDepth > rDepth) {
        return lDepth + 1;
    } else {
        return rDepth + 1;
    }
}
