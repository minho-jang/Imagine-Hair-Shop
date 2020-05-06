# -*- encoding: utf8 -*-
import os
import base64
from flask import Flask, request, jsonify, send_file, render_template, views
from flask_restful import Resource, Api
from FaceSwap import face_main
from paramiko import SSHClient,SFTPClient,Transport
import time

app = Flask(__name__)
api = Api(app)

host = "NIPA_GPU_SERVER_URL"
user = "NIPA_GPU_SERVER_USERNAME"
port ="NIPA_GPU_SERVER_PORT"
password = "NIPA_GPU_SERVER_PASSWORD"


root_dir = 'ROOT_DIR'
source_img_dir = os.path.join(root_dir, 'source-image')
target_img_dir = os.path.join(root_dir, 'target-image')
result_img_dir = os.path.join(root_dir, 'result-image')

def name_processing(img_name):
    source_img = os.path.join(source_img_dir, img_name + '.jpg')
    target_img = os.path.join(target_img_dir, 'img7_target.jpg')  # img_name+'_target.jpg')
    result_img = os.path.join(result_img_dir, img_name + '_result.jpg')

    return source_img, target_img, result_img

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

@app.route('/')
def f1():
    ### 스왑만 하고싶을때 쓰삼

    # img_name = os.path.join('SERVER_DIR', img_name)
    # tar_name = os.path.join('SERVER_DIR', tar_name)
    # ret_name = os.path.join('SERVER_DIR','prof.jpg')

    # swap(img_name, tar_name, ret_name)

    # return send_file('prof.jpg', mimetype='image/jpg')
    return render_template('index.html')

@app.route('/post/<img_name>', methods=['POST'])
def f2(img_name):

    client = SSHClient()
    client.load_system_host_keys()
    client.connect(host, username=user, port=port, password=password)

    b64_string = request.form.get('image')

    img_name = img_name + '.jpg'
    source_img = os.path.join(source_img_dir, img_name)
    target_img = os.path.join(target_img_dir, img_name + '_target.jpg')
    result_img = os.path.join(result_img_dir, img_name + '_result.jpg')

    with open(source_img, "wb") as f:
        f.write(base64.b64decode(b64_string))

    #### GPU서버에 사진 요청 ####

    start = time.time()

    sftp = client.open_sftp()
    sftp.put(source_img, STYLEGAN_FFHQ_DIR + source_img)

    stdin, stdout, stderr = client.exec_command(RUN_COMMAND)

    for i in stderr.readlines():
        print(i)

    for i in stdout.readlines():
        print(i)

    # target image 는 현재 경로에 일단 저장
    sftp.get(STYLEGAN_RESULT_IMAGE_PATH, target_img)

    swap(source_img, target_img, result_img)

    client.close()
    sftp.close()

    with open(result_img, "rb") as img:
        enc_str = base64.b64encode(img.read())


    print('time ****', time.time() - start)

    return enc_str

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
