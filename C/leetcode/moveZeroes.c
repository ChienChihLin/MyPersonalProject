void moveZeroes(int* nums, int numsSize){
    int i, j, tmp;
    
    for (i=0; i<numsSize; i++) {
        for (j=i; j<numsSize; j++) {
            if (nums[j] != 0) {
                if (j != 0) {
                    if ((nums[j] != 0) && (nums[j-1] != 0)) {
                    } else {
                        tmp = nums[j-1];
                        nums[j-1] = nums[j];
                        nums[j] = tmp;
                    }
                }
            }
        }
    }
}
