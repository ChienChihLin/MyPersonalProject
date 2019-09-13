clear;
close All;

% ************************ Continuous init variables ***************************
if (0)
    if (1)
        load 'C:\Users\asus\Desktop\Alogorithm\Activity Heart Rate\TROIKA_Matlab_Code\TestData\TEST_S02_T01.mat'
        load 'C:\Users\asus\Desktop\Alogorithm\Activity Heart Rate\TROIKA_Matlab_Code\TrueBPM\TrueBPM\True_S02_T01'
    else
        load 'C:\Users\asus\Desktop\TROIKA_Matlab_Code\DBW1Data\DBW1_T01.mat'
        load 'C:\Users\asus\Desktop\TROIKA_Matlab_Code\TrueBPMDBW1Data\TrueBPM2_DBW1_T01.mat'
    end
else
    if (0)
        load 'C:\Users\asus\Desktop\TROIKA_Matlab_Code\TestData\TEST_S02_T01.mat'
        load 'C:\Users\asus\Desktop\TROIKA_Matlab_Code\TrueBPM\TrueBPM\True_S02_T01'
    else
        load 'C:\Users\asus\Desktop\Alogorithm\Activity Heart Rate\TROIKA_Matlab_Code\DBW1Data\DBW1_T03.mat'
        load 'C:\Users\asus\Desktop\Alogorithm\Activity Heart Rate\TROIKA_Matlab_Code\TrueBPMDBW1Data\TrueBPM_DBW1_T03.mat'
        load 'C:\Users\asus\Desktop\Alogorithm\Activity Heart Rate\TROIKA_Matlab_Code\TrueBPMDBW1Data\TrueBPM2_DBW1_T03.mat'
    end
end

% Need to modify while using other database
PrevBPM = BPM0(1,1);
accfftSmplPoint = 4000;
ppgfftSmplPoint = 800;
%sigSmplRate = 125;
accSmplRate = 100;
ppgSmplRate = 20;
defaultHRMFreq = PrevBPM/60; %0.8547 %1Hz, location index = 33,based on fft point = fftSmplPoint
defaultHRMLoc = round(((defaultHRMFreq*ppgfftSmplPoint)/ppgSmplRate)); %1Hz location, 16
previousHrFreq = defaultHRMFreq;
previousHrLoc = defaultHRMLoc;

PROCESS_CONT = 0;
BPMCount = 0;
Length = length(sig);
Length = round((Length/(ppgSmplRate*2)));
selWindowIndex = 0;
noMatchCount = 0;
noFindFreq = zeros(1,Length);
noFindFreqCount = 0;
noFindBPMFreqCount = 0;
BPMFreq = 0;
fstVerBPMCount = 0;
sndVerBPMCount = 0;
thirdVerBPMCount = 0;
sameBPMCount = 0;
hiBPMCalCount = 0;
lowBPMCalCount = 0;
%rmACCPeakRangCount = 5;
%rmACCRange = 0;
peakSelThr = 0.3;
dominantMAThr = 0.5;
maxSelSigLocNum = 3;
toleranceBPM = 12;  %12 ,much better now, 15
deltaBPM = 33;      %24 ,much better now, 30
bpmTolerance = round((((toleranceBPM/60)*ppgfftSmplPoint)/ppgSmplRate));%6;
bpmTolFactor = 3;   %2
maTolerance = 0;
deltaLoc = round((((deltaBPM/60)*ppgfftSmplPoint)/ppgSmplRate));
errorFlg = 0;
startWinIndx = 1;
endWinIndx = Length-4;  %Length-4
trueBPMSmplPeriod = 1; % unit = s
% ******************************************************************************

% ************************ Preproduce Phi ************************************** 
N = ppgfftSmplPoint-1;   % row number of the dictionary matrix
M = (ppgSmplRate*8)-1;    % column number of the dictionary matrix
deltaF1 = (0.4*ppgfftSmplPoint/ppgSmplRate)-1;
deltaF2 = 2*ppgfftSmplPoint/ppgSmplRate;
phiFstUpLoc = (0.4*ppgfftSmplPoint/ppgSmplRate)+1-deltaF1;
phiFstLowLoc = round((5*ppgfftSmplPoint/ppgSmplRate)+1+deltaF2);
phiSndUpLoc = ppgfftSmplPoint - (phiFstLowLoc-phiFstUpLoc);

% calculate and verify bpm variable
bpmModifyThr = round((((11/60)*ppgfftSmplPoint)/ppgSmplRate));
bpmModifyLoc = round((((4/60)*ppgfftSmplPoint)/ppgSmplRate));  
trendValue = round((((2/60)*ppgfftSmplPoint)/ppgSmplRate));
prevBPMThr = 3;

for m = 0:M
    colIndx = 0;
    for n = 0:N
        if (((n >= phiFstUpLoc) && (n <= phiFstLowLoc)) || ((n >= phiSndUpLoc) && (n <= ppgfftSmplPoint)))
            colIndx = colIndx + 1;
            tmp_Phi((m+1),colIndx) = exp((1i*2*pi*m*n)/N);
        end
    end
end
Phi = tmp_Phi;
% ******************************************************************************

% ************************ Start HR Alogorithm Analyze *************************
acc_sel_point = 0;
for sel_point = ((startWinIndx - 1)*(ppgSmplRate*2)):(ppgSmplRate*2):((ppgSmplRate*2)*(endWinIndx - 1))%((Length-5)*250)
    selWindowIndex = selWindowIndex + 1;
    
    ppg_start_point = sel_point + 1;
    ppg_end_point = (ppgSmplRate*8) + (ppg_start_point-1);
    
    % Load PPG raw data
    if (0)
        dirty_signal(1,:) = sig(1,ppg_start_point:ppg_end_point);
    else
        dirty_signal(1,:) = sig(ppg_start_point:ppg_end_point,1);    %sig(1,ppg_start_point:ppg_end_point);
        checkIsNaN = isnan(dirty_signal);
        for i = 1:length(checkIsNaN)
            if (checkIsNaN(1,i) == 1)
                errorFlg = 1;
                break;
            end
        end
    end
    if (errorFlg == 1)
        break;
    end

    % Load ACC raw data
    acc_start_point = acc_sel_point + 1;
    acc_end_point = (accSmplRate*8) + (acc_start_point-1);
    acc_sel_point = acc_sel_point + (accSmplRate*2);
    for axis = 1:3
        if (0)
            acc_signal(axis,:) = sig((axis+2),acc_start_point:acc_end_point);
        else
            acc_signal(axis,:) = sig(acc_start_point:acc_end_point,(axis+2));
            checkIsNaN = isnan(acc_signal);
            for i = 1:length(checkIsNaN)
                if (checkIsNaN(1,i) == 1)
                    errorFlg = 1;
                    break;
                end
            end
        end
    end
    if (errorFlg == 1)
        break;
    end
    
    % BPfilter, WnLowFeq = LoFeq/fs, WnHighFeq = HiFeq/fs.
    lo = 0.4/ppgSmplRate;   %0.0032;  % normalized low frequency cut-off, lo = 0.4/sigSmplRate = 0.0032
    hi = 5/ppgSmplRate;     %0.04;    % normalized high frequency cut-off, hi = 5/sigSmplRate = 0.04
    ppg_sig_filt = BPFilter(dirty_signal,lo,hi);
    accDomintFreqLocArray = zeros();
    
    for axis = 1:3
        acc_sig_filt = BPFilter(acc_signal(axis,:),lo,hi);
        % Acc signal processed by FFT
        Y = fft(acc_sig_filt,accfftSmplPoint);
        Y(1) = [];
        n = length(Y);
        power = abs(Y(1:floor(n/2))).^2;    %abs(Y(1:floor(n/2)));
        nyquist = 1/2;
        freq = accSmplRate*((1:n/2)/(n/2)*nyquist);
    %{    
        %figure;
        %plot(freq,power)
    %}
    %{
        figure;
        plot(freq,power)
        xlabel('Frequency')
        title('ACC Periodogram')
    %}
        % Choose MAX ACC spectrum peak
        if (1)
            [maxAccFreqPower,maxAccFreqindex] = max(power);
            accFreqLength = length(power);
            accDominantFreqIndx = 0;
            
            for i = 1:accFreqLength
                if power(1,i) > (maxAccFreqPower*dominantMAThr)
                    accDominantFreqIndx = accDominantFreqIndx + 1;
                    accDomintFreqLocArray(axis,accDominantFreqIndx) = i;
                end
            end
        end
    end
    accFreqResolusion = freq(1,2) - freq(1,1);

% ************************ Singular Spectrum Analyze (SSA) *********************
    L = (round((ppgSmplRate*8)*0.40));    %400;    % Choose L value
    N = length(ppg_sig_filt);
    
    if L > N/2;
       L = N-L;
    end
    
    K = N-L+1;    % Choose K value
    X = zeros(L,K);
    groupNum = 0;

    % Step1 : Build trayectory matrix
    for i = 1:L
        X(i,1:K) = ppg_sig_filt(1,i:i+K-1);    %output trajectory matrix
    end
 
    % Step 2: SVD
    S = X*X'; 
    [U,autoval] = eig(S);
    [d,i] = sort(-diag(autoval));  
    d = -d;
    U = U(:,i);
    sev = sum(d);
    V = (X')*U;

    % Select proper grouping number
%{
    figure;
    plot((d./sev)*100),hold on,plot((d./sev)*100,'rx');
    CalGroup = (d./sev)*100;
    title('Singular Spectrum');xlabel('Eigenvalue Number');ylabel('Eigenvalue (% Norm of trajectory matrix retained)')
%}
    if (1)
        eig_len = 1;   
        while (((d(eig_len,1)./sev)*100) >= 0.5)
            eig_len = eig_len + 1;
            groupNum = groupNum + 1;
        end
        groupNumBuf(selWindowIndex,1) = groupNum;
    else
        groupNum = 50;
    end
    
    % Step 3: Start Grouping
    X_group = cell(groupNum,L,K);
    Vt = V' ;
    i = 1;
    groupnumber = 1;
    
    while(i <= groupNum) 
        X_group{groupnumber} = U(:,i)*Vt(i,:);
        groupnumber = groupnumber+1;
        i = i+1;
    end
    
    % Step 4: Reconstruction
    Lp = min(L,K);
    Kp = max(L,K);
    
    % diagonal averaging
    RC = zeros((groupnumber - 1), N);
    groupRcaSig = zeros();
    for i=1:(groupnumber - 1)
        RC(i,:) = hankelize(X_group{i}, Lp, Kp, N);
    end
    
    % CaracterSignal processed by FFT
    removeSignalCount = 0;
    removeSignalBuf = zeros(1,100);
    y = zeros(1,(ppgSmplRate*8));
    for fftNum = 1:(groupnumber - 1)
        Y = fft(RC(fftNum,:),ppgfftSmplPoint);
        Y(1) = [];
        n = length(Y);
        power = abs(Y(1:floor(n/2))); %abs(Y(1:floor(n/2))).^2;
        nyquist = 1/2;
        freq = ppgSmplRate*((1:n/2)/(n/2)*nyquist);
        
        % Find max caracter signal power
        removeSignalIndex = 0;
        [signalFreqPower,signalFreqIndex] = sort(power,'descend');
        [axisNum,accFreqPeakLength] = size(accDomintFreqLocArray);
        
        % Remove signal of acc frequence
        % Find dominant signal freq location of character signal
        sigFreqLocCount = 0;
        sigFreqLocBuf = zeros();
        
        for domSigPowCount = 1:length(power)
            if (power(1,domSigPowCount) > (signalFreqPower(1,1)*0.5))
                sigFreqLocCount = sigFreqLocCount + 1;
                sigFreqLocBuf(1,sigFreqLocCount) = domSigPowCount;
            end
        end

        for sigLocIndx = 1:length(sigFreqLocBuf)
            for axisIndx = 1:axisNum
                for x = 1:accFreqPeakLength
                    if ((sigFreqLocBuf(1,sigLocIndx) <= (accDomintFreqLocArray(axisIndx,x) + maTolerance)) && (sigFreqLocBuf(1,sigLocIndx) >= (accDomintFreqLocArray(axisIndx,x) - maTolerance)))
                        if (1)
                            if (((sigFreqLocBuf(1,sigLocIndx) > (previousHrLoc+bpmTolerance)) || (sigFreqLocBuf(1,sigLocIndx) < (previousHrLoc-bpmTolerance))) && ((sigFreqLocBuf(1,sigLocIndx) > ((2*((previousHrLoc+bpmTolerance)-1))+1)) || (sigFreqLocBuf(1,sigLocIndx) < ((2*((previousHrLoc-bpmTolerance)-1))+1))) )
                                removeSignalIndex = removeSignalIndex + 1;
                            end
                        else
                            removeSignalIndex = removeSignalIndex + 1;
                            break;
                        end
                    end
                end
            end
        end

        % Find dominant signal freq location of character signal
        sigFreqLocCount = 0;
        sigFreqLocBuf = zeros();       
        
        for domSigPowCount = 1:length(power)
            if (power(1,domSigPowCount) > (signalFreqPower(1,1)*0.5))
                sigFreqLocCount = sigFreqLocCount + 1;
                sigFreqLocBuf(1,sigFreqLocCount) = domSigPowCount;
            end
        end

        % Remove signal of acc frequence
        for sigLocIndx = 1:length(sigFreqLocBuf)
            if (((sigFreqLocBuf(1,sigLocIndx) < (previousHrLoc-1 - (bpmTolerance*bpmTolFactor))) || (sigFreqLocBuf(1,sigLocIndx) > (previousHrLoc-1 + (bpmTolerance*bpmTolFactor)))) && ((sigFreqLocBuf(1,sigLocIndx) < (2*(previousHrLoc-1 - (bpmTolerance*bpmTolFactor)))) || (sigFreqLocBuf(1,sigLocIndx) > (2*(previousHrLoc-1 + (bpmTolerance*bpmTolFactor))))))
                removeSignalIndex = removeSignalIndex + 1;
            else
                for axisIndx = 1:axisNum
                    for x = 1:accFreqPeakLength
                    
                        if ((sigFreqLocBuf(1,sigLocIndx) >= (accDomintFreqLocArray(axisIndx,x) - maTolerance)) && (sigFreqLocBuf(1,sigLocIndx) <= (accDomintFreqLocArray(axisIndx,x) + maTolerance))...
                            && ((sigFreqLocBuf(1,sigLocIndx) < (previousHrLoc-1 - bpmTolerance)) || (sigFreqLocBuf(1,sigLocIndx) > (previousHrLoc-1 + bpmTolerance))) && ((sigFreqLocBuf(1,sigLocIndx) < (2*(previousHrLoc-1-bpmTolerance))) || (sigFreqLocBuf(1,sigLocIndx) > (2*(previousHrLoc-1+bpmTolerance)))))

                            removeSignalIndex = removeSignalIndex + 1;
                        end
                    end
                end
            end
        end

        if removeSignalIndex == 0
            removeSignalCount = removeSignalCount + 1;
            y = y + RC(fftNum,:);
        end
    end

% ************************ Temporal Diffirential *******************************
    y = y';
    if (1)
        for i = 1:2
            N = length(y);
            for j = 1:(N-i)
                y(j,1) = (y((j+1),1) - y(j,1));%;/2/(1/sigSmplRate)
            end
            y((N-(i-1)),1) = 0;
        end
    end
    
    if (0)
        % Lowpass filter test
        filtOrder = 2;
        filtWn = 5/sigSmplRate/2;
        [B,A] = BUTTER(filtOrder,filtWn,'low');
        y = filter(B,A,y);
    end
% ******************************************************************************

    if (1)
% ************************ Start Sparse Spectrum Recovery **********************
        X = zeros(ppgfftSmplPoint,1);
        % candidate values for the regularization parameter (lambda)
        % LAMBDA = logspace(-6,-3, 5);
        lambda = 0.1;    %According to TROIKA paper (pp.8)
        p = 0.8;

        % dictionary matrix with columns draw uniformly from the surface of a unit hypersphere
        [foc_X,gamma_ind,gamma_est,count] = MFOCUSS(Phi,y,lambda,'p',p,'max_iters',5,'epsilon',1e-8,'print',0);
        %MFOCUSS(Phi,y,lambda,'p',0.8,'max_iters',800,'epsilon',1e-8,'print',1);

        for i=1:((phiFstLowLoc-phiFstUpLoc)+1)
            X(i+2,1) = foc_X(i,1);
        end
        
        for j=1:((ppgfftSmplPoint-phiSndUpLoc))
            X((j+phiSndUpLoc),1) = foc_X(j+((phiFstLowLoc-phiFstUpLoc)+1),1);
        end

        n = length(X);
        rca_sig_power = abs(X(1:floor(n/2))).^2;   %abs(X(0:floor(n/2)));
        nyquist = 1/2;
        rca_sig_freq = ppgSmplRate*((0:((n/2)-1))/(n/2)*nyquist);
    else
        Y = fft(y,fftSmplPoint);
        Y(1) = [];
        n = length(Y);
        rca_sig_power = (abs(Y(1:floor(n/2))).^2);
        nyquist = 1/2;
        rca_sig_freq = sigSmplRate*((1:n/2)/(n/2)*nyquist);
        %figure;
        %plot(rca_sig_freq,rca_sig_power,'b-'),hold on,
    end
    
    % Find heart rate frequency range between +-0.3Hz
    hrSigLocIndx = 0;
    fndHrSigLocFlg = 0;
    harmonicSigLocIndx = 0;
    fndHarmonicSigLocFlg = 0;
    hrSigLocBuf = zeros(1,maxSelSigLocNum);
    harmonicSigLocBuf = zeros(1,maxSelSigLocNum);
    Ntrend = 0;
    
    % Heart rate signal location
    loLoc = previousHrLoc-deltaLoc;
    hiLoc = previousHrLoc+deltaLoc;
    
    % loLoc must be positive
    if loLoc <= 0
        loLoc = 1;
    end
    
    % HR signal location
    hrLoLoc = loLoc;
    hrHiLoc = hiLoc;
    [sortLocValue,sortLocIndex] = sort(rca_sig_power((loLoc:hiLoc),1),'descend');
    hrSigLocIndx = hrSigLocIndx + 1;
    maxHrSigLocPowValue = sortLocValue(1,1);
    hrSigLocBuf(1,hrSigLocIndx) = (sortLocIndex(1,1) + (loLoc-1));
    
    for x = 2:((hiLoc-loLoc)+1)
        if (sortLocValue(x,1) > (maxHrSigLocPowValue*peakSelThr))
            hrSigLocIndx = hrSigLocIndx + 1;
            hrSigLocBuf(1,hrSigLocIndx) = (sortLocIndex(x,1) + (loLoc-1));
            fndHrSigLocFlg = 1;
            if (hrSigLocIndx == maxSelSigLocNum)
                %fndHrSigLocFlg = 1;
                break;
            end
        end
    end
    
    % Harmonic signal location
    loLoc = 2*((previousHrLoc-deltaLoc)-1)+1; %2*(previousHrLoc-deltaLoc);
    hiLoc = 2*((previousHrLoc+deltaLoc)-1)+1; % 2*(previousHrLoc+deltaLoc);
    if loLoc <= 0
        loLoc = 1;
    end
    harmonicLoLoc = loLoc;
    harmonicHiLoc = hiLoc;
    [sortHarmonicLocValue,sortHarmonicLocIndx] = sort(rca_sig_power((loLoc:hiLoc),1),'descend');

    for x = 1:((hiLoc-loLoc)+1)
        if (sortHarmonicLocValue(x,1) > (maxHrSigLocPowValue*peakSelThr))
            harmonicSigLocIndx = harmonicSigLocIndx + 1;
            harmonicSigLocBuf(1,harmonicSigLocIndx) = (sortHarmonicLocIndx(x,1) + (loLoc-1));
            fndHarmonicSigLocFlg = 1;
            if (harmonicSigLocIndx == maxSelSigLocNum)
                %fndHarmonicSigLocFlg = 1;
                break;
            end
        end
    end
    
if (0)
    figure;
    plot(rca_sig_freq,rca_sig_power,'b-'),hold on
    plot(rca_sig_freq(1,hrLoLoc:hrHiLoc),rca_sig_power(hrLoLoc:hrHiLoc,1),'gx'),hold on
    plot(rca_sig_freq(1,harmonicLoLoc:harmonicHiLoc),rca_sig_power(harmonicLoLoc:harmonicHiLoc,1),'rx');
end

    % Compare three case between first signal location and harmonic signal location
    isHarmonicSig = 0;
    isMtchFstCaseFlg = 0;
    isMtchSndCaseFlg = 0;
    
    % 1st case
    if (fndHarmonicSigLocFlg == 0)
        fstVerBPMCount = fstVerBPMCount + 1
        CurLoc = previousHrLoc;
        isMtchFstCaseFlg = 1;
    end

    % 2nd case
    if (isMtchFstCaseFlg ~= 1)
            hrSigLocLength = length(hrSigLocBuf);
            harmonicSigLocLength = length(harmonicSigLocBuf);
            for c = 1:hrSigLocLength
                for k = 1:harmonicSigLocLength
                    if (hrSigLocBuf(1,c) == round(((harmonicSigLocBuf(1,k)-1)/2)+1))
                            isHarmonicSig = isHarmonicSig + 1;
                            hrLoc(1,isHarmonicSig) = hrSigLocBuf(1,c);
                    end
                end
            end
            if (isHarmonicSig == 1)
                isMtchSndCaseFlg = 1;
                sndVerBPMCount = sndVerBPMCount + 1
                CurLoc = hrLoc(1,1);
            end
    end
    
    % 3rd case
    if ((isMtchFstCaseFlg ~= 1) && (isMtchSndCaseFlg ~= 1))
        thirdVerBPMCount = thirdVerBPMCount + 1
        hrSigLocSubPrevLoc = abs((hrSigLocBuf(1,:) - previousHrLoc));
        harmncSigLocSubPrevLoc = abs((round(((harmonicSigLocBuf(1,:)-1)/2)+1) - previousHrLoc)); %abs((round((harmonicSigLocBuf(1,:)/2)) - previousHrLoc));
        [minHrSubPrevLocValue,minHrSubPrevLocIndx] = min(hrSigLocSubPrevLoc);
        [minHarmonicSubPrevLocValue,minHarmonicSubPrevLocIndx] = min(harmncSigLocSubPrevLoc);
        if (minHrSubPrevLocValue > minHarmonicSubPrevLocValue)
            bpmLoc = round(((harmonicSigLocBuf(1,minHarmonicSubPrevLocIndx)-1)/2)+1); %round((harmonicSigLocBuf(1,minHarmonicSubPrevLocIndx)/2));
        elseif (minHrSubPrevLocValue < minHarmonicSubPrevLocValue)
            bpmLoc = hrSigLocBuf(1,minHrSubPrevLocIndx);
        elseif (minHrSubPrevLocValue == minHarmonicSubPrevLocValue)
            bpmLoc = hrSigLocBuf(1,minHrSubPrevLocIndx);
        end
        CurLoc = bpmLoc;
    end
    
    % Calculate and verify BPM
    % Verify Current BPM and calibration
    if ((CurLoc - previousHrLoc) >= bpmModifyThr)
        CurLoc = previousHrLoc + bpmModifyLoc; %CurLoc - 6
        hiBPMCalCount = hiBPMCalCount + 1;
        hiBPMCalBuf(1,hiBPMCalCount) = selWindowIndex;
    elseif ((CurLoc - previousHrLoc) <= -bpmModifyThr)
        CurLoc = previousHrLoc - bpmModifyLoc; %CurLoc + 6
        lowBPMCalCount = lowBPMCalCount + 1;
        lowBPMCalBuf(1,lowBPMCalCount) = selWindowIndex;
    end
    
    % Calibration delta location and predict BPM
    if (CurLoc == previousHrLoc)
        sameBPMCount = sameBPMCount + 1;
    else
        deltaLoc = round((((deltaBPM/60)*ppgfftSmplPoint)/ppgSmplRate));
        sameBPMCount = 0;
    end
    
    if (sameBPMCount > 2)
        % Calibrate delta location
        deltaLoc = round((((deltaBPM/60)*ppgfftSmplPoint)/ppgSmplRate)) + 4;
        
        % Calculate predeict BPM
        if (BPMCount < 20)
            tmpBPMBuf = BPMBuf(1,1:BPMCount);
            
            for x = 1:BPMCount
                timeBuf(1,x) = x;
            end
            
            p = polyfit(timeBuf,tmpBPMBuf,3);
            predictBPM = polyval(p,(BPMCount+1))
    
            if ((predictBPM - PrevBPM) >= prevBPMThr)
                Ntrend = 1;%trendValue;
            elseif  ((predictBPM - PrevBPM) <= -prevBPMThr)
                Ntrend = -1;%-trendValue;
            end
        else
            tmpBPMBuf = BPMBuf(1,((BPMCount-20)+1):BPMCount);
 
            for x = 1:20
                timeBuf(1,x) = x;
            end
            
            p = polyfit(timeBuf,tmpBPMBuf,3);
            predictBPM = polyval(p,(20+1))
       
            if ((predictBPM - PrevBPM) >= prevBPMThr)
                Ntrend = 1;%trendValue;
            elseif ((predictBPM - PrevBPM) <= -prevBPMThr)
                Ntrend = -1;%-trendValue;
            end
        end
        CurLoc = previousHrLoc + (2*Ntrend);
    end
    
    % Calculate BPM based on current location
    CurBPM = rca_sig_freq(1,CurLoc)*60
    previousHrLoc
    CurLoc
    BPMCount = BPMCount + 1;
    BPMBuf(1,BPMCount) = CurBPM;%freq(1,maxSVDSignalPowerIndex)*60;
    
    % Store previous BPM frequency
    PrevBPM = CurBPM;
    previousHrLoc = CurLoc;
    
    if (abs(BPM0(selWindowIndex,1)-BPMBuf(1,BPMCount)) > 10)
        noMatchCount = noMatchCount + 1;
        noMatchBuf(1,noMatchCount) = selWindowIndex;
    end
    
PROCESS_CONT = PROCESS_CONT + 1;
PROCESS_CONT
end

if (1)
    % heart rate variable
    aveBPMBuf = zeros();
    aveBPMCount = 0;
    figure;
    if (0)
        plot(BPMBuf,'b-'),hold on,plot((BPM0(startWinIndx:endWinIndx,1)),'r-');
    else
        plot(BPMBuf,'b-'),hold on,plot((BPM0((4/trueBPMSmplPeriod):(2/trueBPMSmplPeriod):length(BPM0),1)),'r-'),hold on,plot((Qual_BPM0((4/trueBPMSmplPeriod):(2/trueBPMSmplPeriod):length(Qual_BPM0),1)),'g-');
    end
    title('BPM Compare');xlabel('Number');ylabel('BPM')
    
    % Average heart rate
    for k = 1:(length(BPMBuf)-3)
        aveBPMCount = aveBPMCount + 1;
        aveBPMBuf(1,aveBPMCount) = (BPMBuf(1,k+3)+BPMBuf(1,k+2)+BPMBuf(1,k+1)+BPMBuf(1,k))/4;
    end
    
    % Calculate average abs error
    totlAbsError = 0;
    persentAbsError = 0;
    
    for j = 1:length(aveBPMBuf)
        totlAbsError = abs(aveBPMBuf(1,j) - BPM0(j,1)) + totlAbsError;
        persentAbsError = abs(aveBPMBuf(1,j) - BPM0(j,1))/BPM0(j,1) + persentAbsError;
    end
    
    avrgeAbsError = totlAbsError/length(aveBPMBuf)
    avrgepersentAbsError = persentAbsError/length(aveBPMBuf)
    
    % Plot different between true HR and estimate HR
    figure;
    if (0)
        plot(aveBPMBuf,'b-'),hold on,plot((BPM0(startWinIndx:endWinIndx,1)),'r-');
    else
        plot(aveBPMBuf,'b-'),hold on,plot((BPM0((4/trueBPMSmplPeriod):(2/trueBPMSmplPeriod):length(BPM0),1)),'r-'),hold on,plot((Qual_BPM0((4/trueBPMSmplPeriod):(2/trueBPMSmplPeriod):length(Qual_BPM0),1)),'g-'), legend('TROIKA BPM', 'True BPM', 'Qualcomm BPM');
    end
    title('BPM Compare');xlabel('Number');ylabel('BPM')
end