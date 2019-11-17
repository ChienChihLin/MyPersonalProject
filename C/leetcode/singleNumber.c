void sort(int* arr, int sSize) {
	int i, j, tmp;

	for (i=0; i < sSize; i++) {
		for (j=0; j<(sSize-i-1); j++) {
			if (arr[j] > arr[j+1]) {
				tmp = arr[j];
				arr[j] = arr[j+1];
				arr[j+1] = tmp;
			}
		}
	}
}

int singleNumber(int* nums, int numsSize){
    int k, diff, sigNum;
    
    if (numsSize == 1) {
        return sigNum = nums[0];
    }
    
    sort(nums, numsSize);
    
    for (k=0; k<(numsSize-1); k++) {
        if (k == 0) {
            if (nums[k] != nums[k+1]) {
                sigNum = nums[k];
            }
        } else if ((k+1) == (numsSize-1)) {
            if (nums[k] != nums[k+1]) {
                sigNum = nums[k+1];
            }
        } else {
            if (nums[k] != nums[k+1]) {
                diff++;
            } else {
                diff = 0;
            }
                
            if (diff == 2) {
                sigNum = nums[k];
            }
        }
    }

    return sigNum;
}
