config.module.rules.push({
    test: /\.css$/,
    include: [
        require.resolve('bootstrap/dist/css/bootstrap.min.css')
    ],
    use: ['style-loader', 'css-loader']
});