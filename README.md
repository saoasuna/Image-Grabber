# Image-Grabber

Image Grabber is an Android application that fetches and displays photos in a smooth, scrollable view. The app connects to Flickr and Booru imageboards using their respective API’s and downloads images according to the SearchView. It compares each photo’s resolution to the device’s screensize to determine the best display size of each picture. The AsyncTask class is used to download images in a background thread and update the UI when available. There is also a desktop script written in Ruby (using the Nokogiri gem) that allows users to batch download these pictures.
