/**
 * Note: The returned array must be malloced, assume caller calls free().
 */
int* twoSum(int* nums, int numsSize, int target, int* returnSize){
    int* ptr;
    int i, j;
    
    ptr = (int*)malloc(2*sizeof(int));

    for (i = 0; i < numsSize; i++) {
        for (j = (numsSize-1); j > i; j--) {
            if ((nums[i] + nums[j]) == target) {
                ptr[0] = i;
                ptr[1] = j;
                
                break;
            }
        }
    }
    
    *returnSize = 2;

    return ptr;
}
