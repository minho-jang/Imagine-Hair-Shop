# -*- encoding: utf8 -*-
import os
import base64
from flask import Flask, request, jsonify, send_file
from flask_restful import Resource, Api
from FaceSwap import face_main

app = Flask(__name__)
api = Api(app)


class FaceSwap(Resource):
    def __init__(self):
        self.root_dir = 'ROOT_DIR'
        self.source_img_dir = os.path.join(self.root_dir, 'source-image')
        self.target_img_dir = os.path.join(self.root_dir, 'target-image')
        self.result_img_dir = os.path.join(self.root_dir, 'result-image')

    def get(self, img_name):  # return saved image by source_img name
        print("GET")
        source_img, target_img, result_img = self.name_processing(img_name)

        # face swap 진행
        self.swap(img_name)
        print(result_img)
        return send_file(result_img, mimetype='image/jpg')

    def post(self, img_name):  # insert new image
        print("POST")
        source_img, target_img, result_img = self.name_processing(img_name)
        print(source_img)

        data = request.get_json()

        if data is None():
            print("No valid request body, json missing!")
            return jsonify("error: No valid request body, json missing!")
        else:
            b64_string = data['image']
            with open(source_img, "wb") as f:
                f.write(base64.decodebytes(b64_string.encode()))

            #### GPU서버에 사진 요청 ####

    def put(self, img_name):  # Chage saved image by source_img name
        return 'change saved image'

    def delete(self, img_name):  # Delete saved image by source_img name
        return 'delete image by source_img'

    def swap(self, img_name):  # Do face swap & return result image
        image_info = {}
        source_img, target_img, result_img = self.name_processing(img_name)

        image_info['src'] = source_img
        image_info['dst'] = target_img
        image_info['out'] = result_img
        image_info['warp_2d'] = False
        image_info['correct_color'] = True
        image_info['no_debug_window'] = True

        face_main.faceswap(image_info)

        return result_img

    def name_processing(self, img_name):
        source_img = os.path.join(self.source_img_dir, img_name + '.jpg')
        target_img = os.path.join(self.target_img_dir, 'img8_target.jpg')  # img_name+'_target.jpg')
        result_img = os.path.join(self.result_img_dir, img_name + '_result.jpg')

        return source_img, target_img, result_img


api.add_resource(FaceSwap, '/face/<string:img_name>')
api.add_resource(FaceSwap, '/face/<string:img_name>/done')

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
