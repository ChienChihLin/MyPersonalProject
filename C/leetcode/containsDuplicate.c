int compare(const void *a, const void *b)
{
      int c = *(int *)a;
      int d = *(int *)b;
      if(c < d) {return -1;}
      else if (c == d) {return 0;}
      else return 1;
}

bool containsDuplicate(int* nums, int numsSize){
    int i, j;
    qsort(nums, numsSize, sizeof(int), compare);
    
    for (i=0; i<numsSize; i++) {
        for (j=i+1; j<numsSize; j++) {
            if (nums[i] == nums[j]) {
                return true;
            } else {
                break;
            }
        }
    }
    
    return false;
}
