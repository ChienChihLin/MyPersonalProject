int compare(const void *a, const void *b)
{
      int c = *(int *)a;
      int d = *(int *)b;
      if(c < d) {return -1;}
      else if (c == d) {return 0;}
      else return 1;
}

int majorityElement(int* nums, int numsSize){
    int i, j;
    int cnt = 1;
    
    qsort(nums, numsSize, sizeof(int), compare);
    
    if (numsSize == 1)
        return nums[0];
    
    for (i=0; i<numsSize; i++) {
        for (j=i+1; j<numsSize; j++) {
            if (nums[i] == nums[j]) {
                cnt++;
                if (cnt > (numsSize/2))
                    return nums[i];
            } else {
                cnt = 1;
                break;
            }
        }
    }
    
    return -1;
}
