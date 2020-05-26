# -*- encoding: utf8 -*-
import base64
from flask import Flask, render_template
from flask_restful import Api, request
from paramiko import SSHClient
import os
from FaceSwap import face_main
from celery import Celery

# celery 동작 명령어
# celery worker -A flaskblog.celery --loglevel=INFO --pool=solo

def make_celery(app):
    celery = Celery(
        "flaskblog",
        backend=app.config['CELERY_RESULT_BACKEND'],
        broker=app.config['CELERY_BROKER_URL']
    )

    class ContextTask(celery.Task):
        def __call__(self, *args, **kwargs):
            with app.app_context():
                return self.run(*args, **kwargs)

    celery.Task = ContextTask
    return celery

# 기존 코드
app = Flask(__name__)

# celery를 위한 추가 코드
app.config.update(
    CELERY_BROKER_URL='redis://localhost:6379/0',
    CELERY_RESULT_BACKEND='redis://localhost:6379/0'
)
celery = make_celery(app)

api = Api(app)

host = "NIPA_GPU_SERVER_URL"
user = "NIPA_GPU_SERVER_USERNAME"
port ="NIPA_GPU_SERVER_PORT"
password = "NIPA_GPU_SERVER_PASSWORD"


root_dir = 'ROOT_DIR'
source_img_dir = os.path.join(root_dir, 'source-image')
target_img_dir = os.path.join(root_dir, 'target-image')
result_img_dir = os.path.join(root_dir, 'result-image')

def swap(source_img,target_img,result_img):  # Do face swap & return result image

    image_info = {}

    image_info['src'] = source_img
    image_info['dst'] = target_img
    image_info['out'] = result_img
    image_info['warp_2d'] = False
    image_info['correct_color'] = True
    image_info['no_debug_window'] = True

    face_main.faceswap(image_info)

    return result_img

# flask 내에서 테스트를 하기 위한 라우트. test.png 로 post 요청 보냄
@app.route('/')
def f1():
    return render_template('index.html')

## 스왑만 하고싶을때 쓰삼
@app.route('/swap/<img_name>')
def onlySwap(img_name):
    sour_name = os.path.join(source_img_dir, img_name + '.png')
    tar_name = os.path.join(target_img_dir, img_name + '.png')
    ret_name = os.path.join(result_img_dir, img_name + '.png')

    swap(sour_name, tar_name, ret_name)

    return render_template("swap.html",
                           sour_name='images/source-image/' + img_name + '.png',
                           tar_name='images/target-image/' + img_name + '.png',
                           ret_name='images/result-image/' + img_name + '.png')


@app.route('/start/<img_name>', methods=['POST'])
def start_synthesis(img_name):

    ## 테스트할 때는 주석 처리
    ##########################################################
    b64_string = request.form.get('image')

    source_img = os.path.join(source_img_dir, img_name + '.jpg')

    with open(source_img, "wb") as f:
        f.write(base64.b64decode(b64_string))

    ##########################################################


    task = execute_gpu.delay(img_name)
    print('post return ', task.id)
    return task.id

# 안드로이드 상에서 결과 이미지 확인을 하기 위한 post 함수. 이미지 존재 시 이미지 인코딩 문자열 리턴, 없을 시 '0' 리턴
@app.route('/result/<img_name>', methods=['POST','GET'])
def get_result(img_name):
    if os.path.isfile(os.path.join(result_img_dir, img_name+'_result.png')):
        with open(os.path.join(result_img_dir, img_name+'.png'), "rb") as img:
            enc_str = base64.b64encode(img.read())
        print('task completed')
        os.remove(os.path.join(result_img_dir, img_name+'.png'))
        return enc_str
    print('task not completed')
    return '0'

# gpu 서버 접속, 실행하는 함수
@celery.task
def execute_gpu(img_name):
    client = SSHClient()
    client.load_system_host_keys()
    client.connect(host, username=user, port=port, password=password)
    sftp = client.open_sftp()

    source_img = os.path.join(source_img_dir, img_name + '.jpg')
    target_img = os.path.join(target_img_dir, img_name + '.png')
    result_img = os.path.join(result_img_dir, img_name + '.png')

    sftp.put(source_img, STYLEGAN_FFHQ_DIR + img_name)

    stdin, stdout, stderr = client.exec_command(RUN_COMMAND)

    # 오류 발생 시 출력
    for i in stderr.readlines():
        print(i)

    sftp.get(STYLEGAN_RESULT_IMAGE_PATH, target_img)

    swap(source_img, target_img, result_img)

    client.close()
    sftp.close()


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
