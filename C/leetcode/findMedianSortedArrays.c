int compare(const void *arg1, const void *arg2) {
    return  (*(int *)arg1 - *(int *)arg2);
}

double findMedianSortedArrays(int* nums1, int nums1Size, int* nums2, int nums2Size){
    int *numArr = (int*)malloc((nums1Size + nums2Size) * sizeof(int));
    int i;
    
    memset(numArr, 0, (nums1Size + nums2Size) * sizeof(int));
    
    for (i = 0; i < nums1Size; i++) {
		numArr[i] = nums1[i];
	}
    
    for (i = 0; i < nums2Size; i++) {
		numArr[nums1Size+i] = nums2[i];
	}
    
    qsort((void *)numArr, (nums1Size + nums2Size), sizeof(int), compare);
    
    if (((nums1Size + nums2Size) % 2) == 0) {
        return (double)(numArr[(nums1Size + nums2Size)/2] + numArr[((nums1Size + nums2Size)/2) - 1])/2.0; 
    } else {
        return (double)numArr[(nums1Size + nums2Size)/2];
    }
}
