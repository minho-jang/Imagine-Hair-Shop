# Imagine-Hair-Shop (상상이발소)
헤어스타일 시뮬레이션 앱

> 경기대학교 컴퓨터공학부 캡스톤디자인 <br>
> 팀명: 나비보벳따우 뽀보벳띠 빠삐벳뽀 <br>
> 프로젝트명: 상상이발소 <br>
> 본 프로젝트는 NIPA의 지원을 받아 제작되었습니다. <br>

기존 헤어스타일 시뮬레이션 앱들은 사용자가 헤어 스티커를 사진에 붙힘으로써 인위적인 사진이 만들어 졌다면, 본 프로젝트는 GAN(Generative Adversarial Network)과 딥러닝 모델 Face swap을 활용하여 인위적인 모습을 자연스럽게 변환시켜 사진 속 헤어스타일을 마치 자신의 실제 헤어스타일이 된 것처럼 만든다.

## Demo
<img src="android/images/before_styling2.jpg" width="32%">  <img src="android/images/after_styling.jpg" width="32%">  <img src="android/images/after_process.jpg" width="32%">

## How?
### 자체적인 데이터셋과 AI모델 구축
기존의 StyleGAN2 모델을 실제 이미지로 테스트했을 때, 성능이 상당히 저조하다. 이는 학습된 데이터셋(FFHQ, Flickr-Faces-HQ Dataset)의 문제로, 학습된 데이터가 대부분 서양인으므로 그에 편향된 스타일이 추출되었다. 따라서 크롤링을 통해 모델이 요구하는 조건에 합당한 이미지 6,000장을 선별하여 재학습 시켰고, 훨씬 향상된 성능을 보였다.
  
### FaceSwap을 통해 이미지 속 이질감 제거
만들어진 가상의 이미지는 스타일은 잘 추출되었지만, 이미지속 얼굴에 변형을 일으켜 거부감을 유발시킨다. 오픈 소스 FaceSwap을 통해서 기존의 얼굴을 가상 이미지에 이식한 후에, 경계선 부분은 가우시안 블러처리를 통해 자연스러운 이미지를 만들어냈다.
  
### 백그라운드로 실행시켜 사용성 향상
위의 이미지 처리 시간이 약 3분으로 너무 오래걸린다. 앱 동작 중 이 처리 시간을 기다리는 방식에서 백그라운드로 응답을 받아서 저장하는 방식으로 설계를 변경했다.

## YouTube
- https://www.youtube.com/watch?v=IcBrkL0-9xU

## References
- https://github.com/NVlabs/stylegan2
- https://github.com/wuhuikai/FaceSwap
